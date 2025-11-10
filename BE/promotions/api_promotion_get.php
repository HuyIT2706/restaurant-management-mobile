<?php
// File: api_promotion_get.php (Để lấy chi tiết một khuyến mãi cho hình 2)
header('Content-Type: application/json');
include('../database.php');
include('../auth.php'); 
error_reporting(E_ALL);
ini_set('display_errors', 1);

// CHẠY MIDDLEWARE XÁC MINH TOKEN
$user_data = verifyToken();

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $promo_id = filter_var($_GET['id'] ?? null, FILTER_VALIDATE_INT);

    if (empty($promo_id)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Vui lòng cung cấp ID khuyến mãi hợp lệ']);
        exit();
    }

    $sql = "SELECT * FROM PROMOTIONS WHERE promo_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $promo_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $promotion = $result->fetch_assoc();
        http_response_code(200);
        echo json_encode(['success' => true, 'promotion' => $promotion]);
    } else {
        http_response_code(404);
        echo json_encode(['success' => false, 'message' => 'Không tìm thấy khuyến mãi']);
    }

    $stmt->close();
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ.']);
}

$conn->close();
?>