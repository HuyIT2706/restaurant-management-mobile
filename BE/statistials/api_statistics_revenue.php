<?php
// =============================================================================
// File: api_statistics_revenue.php
// Mục đích: Thống kê doanh thu theo món ăn (số lượng bán + tổng tiền)
// Dùng cho: Statistics → Bảng "Doanh thu các món"
// Thư mục: BE/statistics/
// =============================================================================

header('Content-Type: application/json; charset=utf-8');
include('../database.php');
include('../auth.php');

$user_data = verifyToken();

// ======= Lấy dữ liệu doanh thu từng món =======
$sql = "
    SELECT 
        p.product_id,
        p.product_name,
        p.image_url,
        SUM(od.order_detail_quantity) AS total_quantity,
        SUM(od.order_detail_quantity * od.order_detail_price) AS total_revenue
    FROM order_details od
    JOIN products p ON od.product_id = p.product_id
    GROUP BY p.product_id, p.product_name, p.image_url
    ORDER BY total_revenue DESC
";

$result = $conn->query($sql);
$items = [];

// ======= Xử lý từng dòng =======
while ($row = $result->fetch_assoc()) {
    $row['total_quantity'] = (int)$row['total_quantity'];
    $row['total_revenue'] = number_format($row['total_revenue'], 0, ',', '.') . ' đ';

    // ======= Chuẩn hoá đường dẫn ảnh =======
    $imageUrl = $row['image_url'] ?? '';

    if (!empty($imageUrl)) {
        // Nếu có chứa "localhost" thì thay bằng IP thật
        $imageUrl = str_replace('http://localhost', 'http://10.111.17.241', $imageUrl);

        // Nếu chỉ là tên file (chưa có http)
        if (!preg_match('/^https?:\/\//', $imageUrl)) {
            $basePath = "http://10.111.17.241/BeMobie/restaurant-management-mobile/BE/assets/products/";
            $imageUrl = $basePath . ltrim($imageUrl, '/');
        }
    }

    $row['image_url'] = $imageUrl ?: null;
    $items[] = $row;
}

// ======= Trả về JSON =======
echo json_encode([
    'success' => true,
    'items' => $items
], JSON_UNESCAPED_UNICODE);

$conn->close();
?>
