<?php
// File: api_promotion_list.php (Để lấy danh sách khuyến mãi cho hình 1)
header('Content-Type: application/json');
include('../database.php');
include('../auth.php'); 
error_reporting(E_ALL);
ini_set('display_errors', 1);

// CHẠY MIDDLEWARE XÁC MINH TOKEN (Quản lý hoặc các role khác có thể xem)
$user_data = verifyToken();

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $sql = "SELECT * FROM PROMOTIONS ORDER BY promo_start_date DESC";
    $result = $conn->query($sql);

    $promotions = [];
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $promotions[] = $row;
        }
    }

    http_response_code(200);
    echo json_encode(['success' => true, 'promotions' => $promotions]);
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ.']);
}

$conn->close();
?>