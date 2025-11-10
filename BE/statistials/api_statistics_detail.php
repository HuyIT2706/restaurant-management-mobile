<?php
// =============================================================================
// File: api_statistics_detail.php
// Mục đích: Thống kê chi tiết bán món theo từng đơn hàng (dựa trên product_id)
// Dùng cho: Statistials → Nhấn nút "Chi tiết" ở bảng doanh thu món
// Thư mục: BE/statistials/
// =============================================================================

header('Content-Type: application/json');
include('../database.php');
include('../auth.php');

$user_data = verifyToken();

// Lấy product_id từ GET (giữ tên tham số cũ để dễ dùng)
$product_id = filter_var($_GET['product_id'] ?? null, FILTER_VALIDATE_INT);
if (!$product_id) {
    echo json_encode(['success' => false, 'message' => 'Thiếu ID món']);
    exit();
}

$sql = "
    SELECT 
        o.order_id,
        od.order_detail_quantity,
        od.order_detail_price,
        DATE_FORMAT(o.order_date, '%d/%m/%Y %H:%i') AS order_date_formatted
    FROM order_details od
    JOIN orders o ON od.order_id = o.order_id
    WHERE od.product_id = ?
    ORDER BY o.order_date DESC
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $product_id);
$stmt->execute();
$result = $stmt->get_result();

$details = [];
while ($row = $result->fetch_assoc()) {
    $row['order_detail_price'] = number_format($row['order_detail_price'], 0, ',', '.') . ' đ';
    $details[] = $row;
}

// Sửa lỗi: "the $details" → "$details"
echo json_encode(['success' => true, 'details' => $details]);

$stmt->close();
$conn->close();
?>