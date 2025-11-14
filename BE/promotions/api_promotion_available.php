<?php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php');
error_reporting(E_ALL);
ini_set('display_errors', 1);

// ✅ Xác minh token
$user_data = verifyToken();

// ✅ Chỉ cho phép GET hoặc POST
$method = $_SERVER['REQUEST_METHOD'];
if ($method !== 'GET' && $method !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ. Vui lòng dùng GET hoặc POST.']);
    exit();
}

// ✅ Lấy tham số order_amount từ query hoặc JSON body
$input = json_decode(file_get_contents('php://input'), true);
$order_amount = filter_var($input['order_amount'] ?? ($_GET['order_amount'] ?? null), FILTER_VALIDATE_FLOAT);

if ($order_amount === false || $order_amount === null || $order_amount < 0) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Vui lòng cung cấp giá trị đơn hàng hợp lệ!'
    ]);
    exit();
}

// ✅ Lấy danh sách promotion có thể áp dụng
$current_date = date('Y-m-d H:i:s');

$sql = "SELECT 
            promo_id,
            promo_code,
            promo_type,
            promo_value,
            promo_quantity,
            promo_desc,
            promo_min_order_amount,
            DATE_FORMAT(promo_start_date, '%Y-%m-%d %H:%i:%s') AS promo_start_date,
            DATE_FORMAT(promo_end_date, '%Y-%m-%d %H:%i:%s') AS promo_end_date,
            promo_active,
            (SELECT COUNT(*) FROM order_promotions WHERE promo_id = promotions.promo_id) AS used_count
        FROM promotions
        WHERE promo_active = 1
            AND ? >= promo_min_order_amount
            AND ? >= promo_start_date
            AND ? <= promo_end_date
            AND (promo_quantity IS NULL OR promo_quantity = 0 OR 
                 (SELECT COUNT(*) FROM order_promotions WHERE promo_id = promotions.promo_id) < promo_quantity)
        ORDER BY 
            CASE 
                WHEN promo_type = 'PhanTram' THEN (promo_value * ? / 100)
                WHEN promo_type = 'SoTien' THEN LEAST(promo_value, ?)
                ELSE 0
            END DESC, 
            promo_start_date DESC";

$stmt = $conn->prepare($sql);
$stmt->bind_param("dssdd", $order_amount, $current_date, $current_date, $order_amount, $order_amount);
$stmt->execute();
$result = $stmt->get_result();

$promotions = [];

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        // Tính toán discount amount
        $discount_amount = 0;
        $promo_type = $row['promo_type'];
        $promo_value = floatval($row['promo_value']);
        
        if ($promo_type === 'PhanTram') {
            $discount_amount = ($order_amount * $promo_value) / 100;
        } else if ($promo_type === 'SoTien') {
            $discount_amount = $promo_value;
            if ($discount_amount > $order_amount) {
                $discount_amount = $order_amount;
            }
        }
        
        $promotions[] = [
            'promo_id' => intval($row['promo_id']),
            'promo_code' => $row['promo_code'],
            'promo_type' => $row['promo_type'],
            'promo_value' => $promo_value,
            'promo_quantity' => intval($row['promo_quantity']),
            'promo_desc' => $row['promo_desc'],
            'promo_min_order_amount' => floatval($row['promo_min_order_amount']),
            'promo_start_date' => $row['promo_start_date'],
            'promo_end_date' => $row['promo_end_date'],
            'promo_active' => intval($row['promo_active']),
            'used_count' => intval($row['used_count']),
            'discount_amount' => round($discount_amount, 2),
            'final_amount' => round($order_amount - $discount_amount, 2)
        ];
    }
}

$stmt->close();

// ✅ Trả phản hồi JSON
http_response_code(200);
echo json_encode([
    'success' => true,
    'count' => count($promotions),
    'promotions' => $promotions
]);

$conn->close();
?>

