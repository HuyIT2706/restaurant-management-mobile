<?php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php');
error_reporting(E_ALL);
ini_set('display_errors', 1);

// ✅ Xác minh token
$user_data = verifyToken();

// ✅ Chỉ cho phép POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    $promo_code = $data['promo_code'] ?? null;
    $order_amount = filter_var($data['order_amount'] ?? null, FILTER_VALIDATE_FLOAT);

    if (empty($promo_code)) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Vui lòng nhập mã khuyến mãi!'
        ]);
        exit();
    }

    if ($order_amount === false || $order_amount === null || $order_amount < 0) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Số tiền đơn hàng không hợp lệ!'
        ]);
        exit();
    }

    // ✅ Truy vấn promotion theo code
    $sql = "SELECT 
                promo_id, 
                promo_code, 
                promo_type, 
                promo_value, 
                promo_quantity, 
                promo_desc, 
                promo_min_order_amount, 
                promo_start_date,
                promo_end_date,
                promo_active,
                (SELECT COUNT(*) FROM order_promotions WHERE promo_id = promotions.promo_id) AS used_count
            FROM promotions
            WHERE promo_code = ?";
    
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("s", $promo_code);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result && $result->num_rows > 0) {
        $promotion = $result->fetch_assoc();
        $stmt->close();
        
        // ✅ Kiểm tra các điều kiện
        
        // 1. Kiểm tra promotion có active không
        if ($promotion['promo_active'] != 1) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Mã khuyến mãi không còn hoạt động!'
            ]);
            exit();
        }
        
        // 2. Kiểm tra thời gian hiệu lực
        $current_date = date('Y-m-d H:i:s');
        if ($current_date < $promotion['promo_start_date']) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Mã khuyến mãi chưa có hiệu lực!'
            ]);
            exit();
        }
        
        if ($current_date > $promotion['promo_end_date']) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Mã khuyến mãi đã hết hạn!'
            ]);
            exit();
        }
        
        // 3. Kiểm tra số lượng còn lại
        $used_count = intval($promotion['used_count']);
        $promo_quantity = intval($promotion['promo_quantity']);
        if ($promo_quantity > 0 && $used_count >= $promo_quantity) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Mã khuyến mãi đã hết số lượng sử dụng!'
            ]);
            exit();
        }
        
        // 4. Kiểm tra giá trị đơn hàng tối thiểu
        $min_order_amount = floatval($promotion['promo_min_order_amount']);
        if ($order_amount < $min_order_amount) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => "Đơn hàng phải có giá trị tối thiểu " . number_format($min_order_amount, 0, ',', '.') . " VND để sử dụng mã khuyến mãi này!",
                'min_order_amount' => $min_order_amount
            ]);
            exit();
        }
        
        // ✅ Tính toán số tiền giảm
        $discount_amount = 0;
        $promo_type = $promotion['promo_type'];
        $promo_value = floatval($promotion['promo_value']);
        
        if ($promo_type === 'PhanTram') {
            // Giảm theo phần trăm
            $discount_amount = ($order_amount * $promo_value) / 100;
        } else if ($promo_type === 'SoTien') {
            // Giảm theo số tiền cố định
            $discount_amount = $promo_value;
            // Đảm bảo không giảm quá số tiền đơn hàng
            if ($discount_amount > $order_amount) {
                $discount_amount = $order_amount;
            }
        }
        
        // ✅ Trả về thông tin promotion và discount
        http_response_code(200);
        echo json_encode([
            'success' => true,
            'message' => 'Mã khuyến mãi hợp lệ!',
            'promotion' => [
                'promo_id' => intval($promotion['promo_id']),
                'promo_code' => $promotion['promo_code'],
                'promo_type' => $promotion['promo_type'],
                'promo_value' => $promo_value,
                'promo_desc' => $promotion['promo_desc'],
                'discount_amount' => round($discount_amount, 2),
                'final_amount' => round($order_amount - $discount_amount, 2)
            ]
        ]);
    } else {
        $stmt->close();
        http_response_code(404);
        echo json_encode([
            'success' => false,
            'message' => 'Mã khuyến mãi không tồn tại!'
        ]);
    }
} else {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Phương thức không được hỗ trợ. Vui lòng dùng POST.'
    ]);
}

$conn->close();
?>

