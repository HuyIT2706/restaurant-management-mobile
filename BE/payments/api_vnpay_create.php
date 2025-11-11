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
    $order_totalamount = null;

    // 1. Lấy order_totalamount từ CSDL (đảm bảo tính chính xác)
    if ($order_id) {
        $sql = "SELECT order_totalamount FROM ORDERS WHERE order_id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("i", $order_id);
        $stmt->execute();
        $result = $stmt->get_result();
        if ($row = $result->fetch_assoc()) {
            $order_totalamount = $row['order_totalamount'];
        }
        $stmt->close();
    }

    if (!$order_totalamount) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Không tìm thấy đơn hàng hoặc số tiền thanh toán.']);
        exit;
    }

    $vnp_TxnRef = $order_id . time(); // Mã tham chiếu duy nhất
    $vnp_OrderInfo = 'Thanh toan don hang ID: ' . $order_id;
    $vnp_OrderType = 'billpayment';
    $vnp_Amount = intval($order_totalamount); // VNPAY nhận số tiền tính bằng VND
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