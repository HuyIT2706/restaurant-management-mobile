<?php
// File: api_order_details_by_table.php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php'); 
error_reporting(E_ALL);
ini_set('display_errors', 1);

$user_data = verifyToken();

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $table_id = filter_var($_GET['table_id'] ?? null, FILTER_VALIDATE_INT);

    if (empty($table_id)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Vui lòng cung cấp ID bàn hợp lệ']);
        exit();
    }

    $sql = "
        SELECT 
            p.product_name,
            od.order_detail_quantity,
            od.order_detail_price,
            o.order_date
        FROM orders o
        JOIN order_details od ON o.order_id = od.order_id
        JOIN products p ON od.product_id = p.product_id
        WHERE o.table_id = ?
        ORDER BY o.order_date DESC
    ";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $table_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $details = [];
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $details[] = $row;
        }
    }

    http_response_code(200);
    echo json_encode(['success' => true, 'details' => $details]);
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ.']);
}

$conn->close();
?>