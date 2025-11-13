<?php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php');
error_reporting(E_ALL);
ini_set('display_errors', 1);

// ✅ Xác minh token
$user_data = verifyToken();

// ✅ Chỉ cho phép GET và POST (để tương thích với nhiều client)
$method = $_SERVER['REQUEST_METHOD'];
if ($method === 'GET' || $method === 'POST') {

    // ✅ Lấy ID từ JSON body, POST hoặc query string
    $input = json_decode(file_get_contents('php://input'), true);
    $promo_id = $input['promo_id'] ?? ($_POST['promo_id'] ?? ($_GET['id'] ?? null));
    $promo_id = filter_var($promo_id, FILTER_VALIDATE_INT);

    if (empty($promo_id)) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Vui lòng cung cấp ID khuyến mãi hợp lệ!'
        ]);
        exit();
    }

    // ✅ Truy vấn dữ liệu khuyến mãi
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
                promo_active
            FROM promotions
            WHERE promo_id = ?";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $promo_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result && $result->num_rows > 0) {
        $promotion = $result->fetch_assoc();
        http_response_code(200);
        echo json_encode([
            'success' => true,
            'promotion' => $promotion
        ]);
    } else {
        http_response_code(404);
        echo json_encode([
            'success' => false,
            'message' => "Không tìm thấy khuyến mãi có ID = $promo_id!"
        ]);
    }

    $stmt->close();
} else {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Phương thức không được hỗ trợ. Vui lòng dùng GET hoặc POST.'
    ]);
}

$conn->close();
?>
