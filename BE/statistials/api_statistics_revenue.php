<?php
// =============================================================================
// File: api_statistics_revenue.php
// Mục đích: Thống kê doanh thu theo món ăn (số lượng bán + tổng tiền)
// Dùng cho: Statistics → Bảng "Doanh thu các món"
// Thư mục: BE/statistials/
// =============================================================================

header('Content-Type: application/json; charset=utf-8');

include('../database.php');
include('../auth.php');

// Xác thực token
$user_data = verifyToken();

// Kiểm tra quyền: Chỉ cho phép Quản lý (QuanLy) xem thống kê
if ($user_data->user_role !== 'QuanLy') {
    http_response_code(403); // Forbidden
    echo json_encode([
        'success' => false, 
        'message' => 'Bạn không có quyền xem thống kê.'
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

// ======= Lấy dữ liệu doanh thu từng món =======
$sql = "
SELECT
    p.product_id,
    p.product_name,
    p.image_url,
    COALESCE(SUM(od.order_detail_quantity), 0) AS total_quantity,
    COALESCE(SUM(od.order_detail_quantity * od.order_detail_price), 0) AS total_revenue
FROM products p
LEFT JOIN order_details od ON p.product_id = od.product_id
GROUP BY p.product_id, p.product_name, p.image_url
ORDER BY total_revenue DESC
";

$result = $conn->query($sql);

if (!$result) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi khi truy vấn dữ liệu: ' . $conn->error
    ], JSON_UNESCAPED_UNICODE);
    $conn->close();
    exit();
}

$items = [];

// ======= Xử lý từng dòng =======
while ($row = $result->fetch_assoc()) {
    // Đảm bảo các trường có giá trị mặc định nếu null
    $productId = (int)($row['product_id'] ?? 0);
    $productName = $row['product_name'] ?? 'Không có tên';
    $imageUrl = $row['image_url'] ?? null;
    $totalQuantity = (int)($row['total_quantity'] ?? 0);
    $totalRevenue = (float)($row['total_revenue'] ?? 0);
    
    // Format revenue với dấu chấm phân cách hàng nghìn
    $formattedRevenue = number_format($totalRevenue, 0, ',', '.') . ' đ';
    
    // ======= Chuẩn hoá đường dẫn ảnh =======
    if (!empty($imageUrl)) {
        $imageUrl = str_replace('http://localhost', 'https://onefood.id.vn', $imageUrl);
        // Nếu ảnh chỉ là tên file, thêm đường dẫn gốc đầy đủ
        if (!preg_match('/^https?:\/\//', $imageUrl)) {
            $basePath = "https://onefood.id.vn/BE/assets/products/";
            $imageUrl = $basePath . ltrim($imageUrl, '/');
        }
    } else {
        $imageUrl = null;
    }
    
    $items[] = [
        'product_id' => $productId,
        'product_name' => $productName,
        'image_url' => $imageUrl,
        'total_quantity' => $totalQuantity,
        'total_revenue' => $formattedRevenue
    ];
}

// ======= Trả về JSON =======
echo json_encode([
    'success' => true,
    'items' => $items
], JSON_UNESCAPED_UNICODE);

$conn->close();

?>

