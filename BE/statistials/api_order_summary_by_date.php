<?php
// File: api_order_summary_by_date.php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php'); 
error_reporting(E_ALL);
ini_set('display_errors', 1);

$user_data = verifyToken();

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $start_date = $_GET['start_date'] ?? date('Y-m-d 00:00:00');
    $end_date = $_GET['end_date'] ?? date('Y-m-d 23:59:59');

    $sql = "
        SELECT 
            o.order_date,
            u.user_firstname,
            u.user_lastname,
            SUM(od.order_detail_quantity * od.order_detail_price) as total_amount
        FROM orders o
        JOIN order_details od ON o.order_id = od.order_id
        JOIN users u ON o.user_id = u.user_id
        WHERE o.order_date BETWEEN ? AND ?
        GROUP BY o.order_date, u.user_firstname, u.user_lastname
        ORDER BY o.order_date DESC
    ";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ss", $start_date, $end_date);
    $stmt->execute();
    $result = $stmt->get_result();

    $summary = [];
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $summary[] = $row;
        }
    }

    http_response_code(200);
    echo json_encode(['success' => true, 'summary' => $summary]);
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ.']);
}

$conn->close();
?>