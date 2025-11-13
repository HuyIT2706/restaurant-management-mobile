<?php
// =============================================================================
// File: api_statistics_detail.php
// Mục đích: Lấy chi tiết doanh thu của 1 sản phẩm cụ thể
// Dùng cho: Statistics → Chi tiết sản phẩm
// Thư mục: BE/statistials/
// =============================================================================

header('Content-Type: application/json; charset=utf-8');

include('../database.php');
include('../auth.php');

// Xác thực token
try {
    $user_data = verifyToken();
} catch (Exception $e) {
    http_response_code(401);
    echo json_encode([
        'success' => false, 
        'message' => 'Token không hợp lệ: ' . $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

// Kiểm tra quyền: Chỉ cho phép Quản lý (QuanLy) xem thống kê
if ($user_data->user_role !== 'QuanLy') {
    http_response_code(403); // Forbidden
    echo json_encode([
        'success' => false, 
        'message' => 'Bạn không có quyền xem thống kê.'
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

// Lấy product_id từ parameter
$product_id = filter_var($_GET['product_id'] ?? null, FILTER_VALIDATE_INT);

if (empty($product_id) || $product_id === false) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'ID sản phẩm không hợp lệ!'
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

// ======= Lấy thông tin sản phẩm (JOIN với categories để lấy category_name) =======
$sql_product = "
SELECT 
    p.product_id,
    p.product_name,
    p.price,
    p.product_description,
    p.image_url,
    c.category_name AS product_category
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
WHERE p.product_id = ?
";

$stmt_product = $conn->prepare($sql_product);
if (!$stmt_product) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi chuẩn bị SQL: ' . $conn->error
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

$stmt_product->bind_param("i", $product_id);
$stmt_product->execute();
$result_product = $stmt_product->get_result();

if ($result_product->num_rows === 0) {
    http_response_code(404);
    echo json_encode([
        'success' => false,
        'message' => 'Không tìm thấy sản phẩm!'
    ], JSON_UNESCAPED_UNICODE);
    $stmt_product->close();
    $conn->close();
    exit();
}

$product = $result_product->fetch_assoc();
$stmt_product->close();

// ======= Lấy thống kê chi tiết =======
$sql_stats = "
SELECT
    DATE(o.order_date) AS order_date,
    SUM(od.order_detail_quantity) AS daily_quantity,
    SUM(od.order_detail_quantity * od.order_detail_price) AS daily_revenue,
    COUNT(DISTINCT o.order_id) AS order_count
FROM order_details od
JOIN orders o ON od.order_id = o.order_id
WHERE od.product_id = ?
GROUP BY DATE(o.order_date)
ORDER BY order_date DESC
LIMIT 30
";

$stmt_stats = $conn->prepare($sql_stats);
if (!$stmt_stats) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi chuẩn bị SQL: ' . $conn->error
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

$stmt_stats->bind_param("i", $product_id);
$stmt_stats->execute();
$result_stats = $stmt_stats->get_result();

$dailyStats = [];
while ($row = $result_stats->fetch_assoc()) {
    $dailyStats[] = [
        'order_date' => $row['order_date'] ?? null,
        'daily_quantity' => (int)($row['daily_quantity'] ?? 0),
        'daily_revenue' => number_format((float)($row['daily_revenue'] ?? 0), 0, ',', '.') . ' đ',
        'order_count' => (int)($row['order_count'] ?? 0)
    ];
}
$stmt_stats->close();

// ======= Tổng hợp thống kê =======
$sql_total = "
SELECT
    COALESCE(SUM(od.order_detail_quantity), 0) AS total_quantity,
    COALESCE(SUM(od.order_detail_quantity * od.order_detail_price), 0) AS total_revenue,
    COUNT(DISTINCT o.order_id) AS total_orders
FROM order_details od
JOIN orders o ON od.order_id = o.order_id
WHERE od.product_id = ?
";

$stmt_total = $conn->prepare($sql_total);
if (!$stmt_total) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi chuẩn bị SQL: ' . $conn->error
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

$stmt_total->bind_param("i", $product_id);
$stmt_total->execute();
$result_total = $stmt_total->get_result();
$total = $result_total->fetch_assoc();
$stmt_total->close();

// ======= Chuẩn hoá đường dẫn ảnh =======
$imageUrl = $product['image_url'] ?? null;
if (!empty($imageUrl)) {
    $imageUrl = str_replace('http://localhost', 'https://onefood.id.vn', $imageUrl);
    if (!preg_match('/^https?:\/\//', $imageUrl)) {
        $basePath = "https://onefood.id.vn/BE/assets/products/";
        $imageUrl = $basePath . ltrim($imageUrl, '/');
    }
}

// ======= Trả về JSON =======
echo json_encode([
    'success' => true,
    'product' => [
        'product_id' => (int)($product['product_id'] ?? 0),
        'product_name' => $product['product_name'] ?? 'Không có tên',
        'product_price' => number_format((float)($product['price'] ?? 0), 0, ',', '.') . ' đ',
        'product_category' => $product['product_category'] ?? null,
        'product_description' => $product['product_description'] ?? null,
        'image_url' => $imageUrl
    ],
    'total_stats' => [
        'total_quantity' => (int)($total['total_quantity'] ?? 0),
        'total_revenue' => number_format((float)($total['total_revenue'] ?? 0), 0, ',', '.') . ' đ',
        'total_orders' => (int)($total['total_orders'] ?? 0)
    ],
    'daily_stats' => $dailyStats
], JSON_UNESCAPED_UNICODE);

$conn->close();

?>

