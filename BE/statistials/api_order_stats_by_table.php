<?php
// File: api_order_stats_by_table.php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php'); 
error_reporting(E_ALL);
ini_set('display_errors', 1);

$user_data = verifyToken();

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $sql = "
        SELECT 
            t.table_id,
            t.table_name,
            SUM(od.order_detail_quantity * od.order_detail_price) as total_amount,
            o.order_date
        FROM tables t
        LEFT JOIN orders o ON t.table_id = o.table_id
        LEFT JOIN order_details od ON o.order_id = od.order_id
        GROUP BY t.table_id, t.table_name, o.order_date
        ORDER BY o.order_date DESC
    ";
    $result = $conn->query($sql);

    $stats = [];
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $stats[] = $row;
        }
    }

    http_response_code(200);
    echo json_encode(['success' => true, 'stats' => $stats]);
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ.']);
}

$conn->close();
?>