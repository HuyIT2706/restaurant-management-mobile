<?php
// =============================================================================
// File: api_statistics_revenue.php
// Mục đích: Thống kê doanh thu theo món ăn (số lượng bán + tổng tiền)
// Dùng cho: Statistials → Bảng "Doanh thu các món"
// Thư mục: BE/statistials/
// =============================================================================

header('Content-Type: application/json');
include('../database.php');
include('../auth.php');

$user_data = verifyToken();

$sql = "
    SELECT 
        p.product_id,
        p.product_name,
        SUM(od.order_detail_quantity) AS total_quantity,
        SUM(od.order_detail_quantity * od.order_detail_price) AS total_revenue
    FROM order_details od
    JOIN products p ON od.product_id = p.product_id
    GROUP BY p.product_id, p.product_name
    ORDER BY total_revenue DESC
";

$result = $conn->query($sql);
$items = [];  // Đổi tên biến: $products → $items (không dùng từ product)

while ($row = $result->fetch_assoc()) {
    $row['total_quantity'] = (int)$row['total_quantity'];
    $row['total_revenue'] = number_format($row['total_revenue'], 0, ',', '.') . ' đ';
    $items[] = $row;
}

echo json_encode(['success' => true, 'items' => $items]);  // Trả về 'items' thay vì 'products'

$conn->close();
?>