<?php
// =============================================================================
// File: api_statistics_detail.php
// Mục đích: Thống kê chi tiết bán món theo từng đơn hàng (dựa trên product_id)
// Dùng cho: RevenueDetailScreen (Kotlin Jetpack Compose)
// Thư mục: BE/statistics/
// =============================================================================

header('Content-Type: application/json; charset=utf-8');
include('../database.php');
include('../auth.php');

$user_data = verifyToken();

// Nhận product_id từ GET
$product_id = filter_var($_GET['product_id'] ?? null, FILTER_VALIDATE_INT);
if (!$product_id) {
    echo json_encode(['success' => false, 'message' => 'Thiếu ID món']);
    exit();
}

// ======= Lấy thông tin sản phẩm (tên món, ảnh, tổng SL, doanh thu) =======
$sqlInfo = "
    SELECT 
        p.product_id,
        p.product_name,
        p.image_url,
        SUM(od.order_detail_quantity) AS total_quantity,
        SUM(od.order_detail_quantity * od.order_detail_price) AS total_revenue
    FROM order_details od
    JOIN products p ON od.product_id = p.product_id
    WHERE od.product_id = ?
    GROUP BY p.product_id, p.product_name, p.image_url
";
$stmtInfo = $conn->prepare($sqlInfo);
$stmtInfo->bind_param("i", $product_id);
$stmtInfo->execute();
$infoResult = $stmtInfo->get_result();
$productInfo = $infoResult->fetch_assoc();

if (!$productInfo) {
    echo json_encode(['success' => false, 'message' => 'Không tìm thấy sản phẩm']);
    exit();
}

// ======= Lấy danh sách đơn hàng chứa món này =======
$sql = "
    SELECT 
        o.order_id,
        od.order_detail_quantity AS quantity,
        od.order_detail_price AS price,
        DATE_FORMAT(o.order_date, '%d/%m/%Y %H:%i') AS timestamp
    FROM order_details od
    JOIN orders o ON od.order_id = o.order_id
    WHERE od.product_id = ?
    ORDER BY o.order_date DESC
";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $product_id);
$stmt->execute();
$result = $stmt->get_result();

$orders = [];
while ($row = $result->fetch_assoc()) {
    $row['price'] = number_format($row['price'], 0, ',', '.') . ' đ';
    $orders[] = $row;
}

// ======= Chuẩn bị đường dẫn ảnh đầy đủ =======
$imageUrl = $productInfo['image_url'] ?? '';

if (!empty($imageUrl)) {
    // Nếu trong DB có 'http://localhost', thay bằng IP thực của máy server (LAN)
    $imageUrl = str_replace('http://localhost', 'http://10.237.138.241', $imageUrl);

    // Nếu ảnh chỉ là tên file, thêm đường dẫn gốc đầy đủ
    if (!preg_match('/^https?:\/\//', $imageUrl)) {
        $basePath = "http://10.237.138.241/BeMobie/restaurant-management-mobile/BE/assets/products/";
        $imageUrl = $basePath . ltrim($imageUrl, '/');
    }
}

// ======= Gửi phản hồi JSON =======
$response = [
    'success' => true,
    'product_id' => (int)$productInfo['product_id'],
    'product_name' => $productInfo['product_name'],
    'image_url' => $imageUrl ?: null,
    'total_quantity' => (int)$productInfo['total_quantity'],
    'total_revenue' => number_format($productInfo['total_revenue'], 0, ',', '.') . ' đ',
    'is_best_seller' => 0,
    'orders' => $orders
];

echo json_encode($response, JSON_UNESCAPED_UNICODE);

$stmt->close();
$stmtInfo->close();
$conn->close();
?>
