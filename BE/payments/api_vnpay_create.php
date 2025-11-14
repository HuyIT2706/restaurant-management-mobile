<?php
header('Content-Type: application/json');
include('../database.php'); // Kết nối CSDL và load .env
include('../auth.php');      // Hàm xác minh JWT Token
error_reporting(E_ALL);
ini_set('display_errors', 1);

// XÁC MINH TOKEN VÀ PHÂN QUYỀN
$user_data = verifyToken();
if ($user_data->user_role !== 'ThuNgan' && $user_data->user_role !== 'QuanLy') {
    http_response_code(403);
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền khởi tạo thanh toán.']);
    exit();
}

$vnp_TmnCode = $_ENV['VNPAY_TMN_CODE'];
$vnp_HashSecret = $_ENV['VNPAY_HASH_SECRET'];
$vnp_Url = $_ENV['VNPAY_URL'];
$vnp_Returnurl = $_ENV['VNPAY_RETURN_URL'];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    $order_id = filter_var($data['order_id'] ?? null, FILTER_VALIDATE_INT);
    $promo_code = $data['promo_code'] ?? null;
    $promo_id = filter_var($data['promo_id'] ?? null, FILTER_VALIDATE_INT);
    $order_totalamount = null;

    // 1. Lấy order_totalamount từ CSDL (đảm bảo tính chính xác)
    if ($order_id) {
        $sql = "SELECT order_totalamount FROM orders WHERE order_id = ? FOR UPDATE";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("i", $order_id);
        $stmt->execute();
        $result = $stmt->get_result();
        if ($row = $result->fetch_assoc()) {
            $order_totalamount = floatval($row['order_totalamount']);
        }
        $stmt->close();
    }

    if (!$order_totalamount || $order_totalamount <= 0) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Không tìm thấy đơn hàng hoặc số tiền thanh toán.']);
        exit;
    }
    
    // 2. Xử lý promotion nếu có (chỉ tính toán, không lưu vào DB ở đây)
    $discount_amount = 0;
    $final_amount = $order_totalamount;
    $promotion_id = null;
    
    if (!empty($promo_code) || !empty($promo_id)) {
        // Validate promotion
        if (!empty($promo_id)) {
            // Sử dụng promo_id nếu có
            $sql_promo = "SELECT 
                            promo_id, 
                            promo_type, 
                            promo_value,
                            promo_quantity,
                            (SELECT COUNT(*) FROM order_promotions WHERE promo_id = promotions.promo_id) AS used_count
                        FROM promotions
                        WHERE promo_id = ? AND promo_active = 1";
            $stmt_promo = $conn->prepare($sql_promo);
            $stmt_promo->bind_param("i", $promo_id);
        } else if (!empty($promo_code)) {
            // Hoặc sử dụng promo_code
            $sql_promo = "SELECT 
                            promo_id, 
                            promo_type, 
                            promo_value,
                            promo_quantity,
                            (SELECT COUNT(*) FROM order_promotions WHERE promo_id = promotions.promo_id) AS used_count
                        FROM promotions
                        WHERE promo_code = ? AND promo_active = 1";
            $stmt_promo = $conn->prepare($sql_promo);
            $stmt_promo->bind_param("s", $promo_code);
        }
        
        $stmt_promo->execute();
        $result_promo = $stmt_promo->get_result();
        
        if ($result_promo && $result_promo->num_rows > 0) {
            $promotion = $result_promo->fetch_assoc();
            $promotion_id = intval($promotion['promo_id']);
            $promo_type = $promotion['promo_type'];
            $promo_value = floatval($promotion['promo_value']);
            
            // Kiểm tra số lượng còn lại
            $used_count = intval($promotion['used_count']);
            $promo_quantity = intval($promotion['promo_quantity']);
            if ($promo_quantity > 0 && $used_count >= $promo_quantity) {
                http_response_code(400);
                echo json_encode(['success' => false, 'message' => 'Mã khuyến mãi đã hết số lượng sử dụng!']);
                exit;
            }
            
            // Tính toán discount
            if ($promo_type === 'PhanTram') {
                // Giảm theo phần trăm
                $discount_amount = ($order_totalamount * $promo_value) / 100;
            } else if ($promo_type === 'SoTien') {
                // Giảm theo số tiền cố định
                $discount_amount = $promo_value;
                // Đảm bảo không giảm quá số tiền đơn hàng
                if ($discount_amount > $order_totalamount) {
                    $discount_amount = $order_totalamount;
                }
            }
            
            $final_amount = $order_totalamount - $discount_amount;
            if ($final_amount < 0) {
                $final_amount = 0;
            }
        }
        $stmt_promo->close();
    }
    
    $order_totalamount = $final_amount;
    
    // 3. Lưu promotion vào order_promotions nếu có (sẽ được áp dụng khi thanh toán thành công)
    // Lưu tạm vào order_promotions với order_id và promo_id (chưa cập nhật order_totalamount)
    if ($promotion_id != null && $discount_amount > 0) {
        // Kiểm tra xem đã có promotion nào cho order này với promo_id này chưa (tránh duplicate)
        $sql_check_promo = "SELECT order_promo_id FROM order_promotions WHERE order_id = ? AND promo_id = ?";
        $stmt_check_promo = $conn->prepare($sql_check_promo);
        $stmt_check_promo->bind_param("ii", $order_id, $promotion_id);
        $stmt_check_promo->execute();
        $result_check_promo = $stmt_check_promo->get_result();
        
        if ($result_check_promo->num_rows == 0) {
            // Chưa có, insert mới
            $sql_order_promo = "INSERT INTO order_promotions (order_id, promo_id, order_promo_amount) VALUES (?, ?, ?)";
            $stmt_order_promo = $conn->prepare($sql_order_promo);
            $stmt_order_promo->bind_param("iid", $order_id, $promotion_id, $discount_amount);
            $stmt_order_promo->execute();
            $stmt_order_promo->close();
        } else {
            // Đã có, update amount
            $sql_update_promo = "UPDATE order_promotions SET order_promo_amount = ? WHERE order_id = ? AND promo_id = ?";
            $stmt_update_promo = $conn->prepare($sql_update_promo);
            $stmt_update_promo->bind_param("dii", $discount_amount, $order_id, $promotion_id);
            $stmt_update_promo->execute();
            $stmt_update_promo->close();
        }
        $stmt_check_promo->close();
    }

    $vnp_TxnRef = $order_id . time(); // Mã tham chiếu duy nhất
    $vnp_OrderInfo = 'Thanh toan don hang ID: ' . $order_id;
    if ($promotion_id) {
        $vnp_OrderInfo .= ' - Khuyen mai: ' . ($promo_code ?? 'ID:' . $promotion_id);
    }
    $vnp_OrderType = 'billpayment';
    $vnp_Amount = intval($order_totalamount); // VNPAY nhận số tiền tính bằng VND (sau khi đã áp dụng discount)
    $vnp_Locale = 'vn';
    $vnp_IpAddr = $_SERVER['REMOTE_ADDR'];
    $vnp_CreateDate = date('YmdHis');

    $inputData = array(
        "vnp_Version" => "2.1.0",
        "vnp_TmnCode" => $vnp_TmnCode,
        "vnp_Amount" => $vnp_Amount * 100,
        "vnp_Command" => "pay",
        "vnp_CreateDate" => $vnp_CreateDate,
        "vnp_CurrCode" => "VND",
        "vnp_IpAddr" => $vnp_IpAddr,
        "vnp_Locale" => $vnp_Locale,
        "vnp_OrderInfo" => $vnp_OrderInfo,
        "vnp_OrderType" => $vnp_OrderType,
        "vnp_ReturnUrl" => $vnp_Returnurl,
        "vnp_TxnRef" => $vnp_TxnRef,
    );

    ksort($inputData);
    $query = "";
    $hashdata = "";
    foreach ($inputData as $key => $value) {
        $query .= urlencode($key) . "=" . urlencode($value) . '&';
        $hashdata .= urlencode($key) . "=" . urlencode($value) . '&';
    }
    
    // Tính hash từ query string (không có vnp_SecureHash và không có dấu & cuối)
    $hashdata = rtrim($hashdata, '&');
    $vnpSecureHash = hash_hmac('sha512', $hashdata, $vnp_HashSecret);
    $vnp_Url = $vnp_Url . "?" . $query . 'vnp_SecureHash=' . $vnpSecureHash;
    
    error_log("VNPay Return URL: " . $vnp_Returnurl);
    error_log("VNPay Order Info: " . $vnp_OrderInfo);
    error_log("VNPay TxnRef: " . $vnp_TxnRef);

    // DEBUG: Ghi log thông tin
    error_log("TMN_CODE: " . $vnp_TmnCode);
    error_log("HASH_SECRET: " . $vnp_HashSecret);
    error_log("Hash Data: " . $hashdata);
    error_log("Secure Hash: " . $vnpSecureHash);
    error_log("Full URL: " . $vnp_Url);

    // Trả về URL VNPAY cho Frontend (Compose)
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'Tạo URL VNPAY thành công',
        'vnpay_url' => $vnp_Url,
        'debug' => [
            'tmn_code' => $vnp_TmnCode,
            'hash_data' => $hashdata,
            'secure_hash' => $vnpSecureHash
        ]
    ]);

} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ.']);
}
$conn->close();
?>