<?php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php');

$user_data = verifyToken(); 

$sql = "
    SELECT 
        o.order_id,
        CONCAT(u.user_firstname, ' ',u.user_lastname ) AS full_name,
        DATE_FORMAT(o.order_date, '%d/%m/%Y %H:%i') AS order_date_formatted,
        COALESCE(p.payment_method, 'Chưa thanh toán') AS payment_method
    FROM orders o
    JOIN users u ON o.user_id = u.user_id
    LEFT JOIN payments p ON o.order_id = p.order_id
    ORDER BY o.order_date DESC
";

$result = $conn->query($sql);
$orders = [];

while ($row = $result->fetch_assoc()) {
    $orders[] = $row;
}

echo json_encode(['success' => true, 'orders' => $orders]);
$conn->close();
?>