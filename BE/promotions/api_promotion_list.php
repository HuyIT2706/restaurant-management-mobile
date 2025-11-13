<?php
// ✅ File: api_promotion_list.php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php');
error_reporting(E_ALL);
ini_set('display_errors', 1);

// ✅ Xác minh token (cho phép mọi vai trò xem danh sách)
$user_data = verifyToken();

// ✅ Chỉ cho phép GET hoặc POST (tương thích client Android / Retrofit / Compose)
$method = $_SERVER['REQUEST_METHOD'];
if ($method !== 'GET' && $method !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ. Vui lòng dùng GET hoặc POST.']);
    exit();
}

// ✅ Lấy tham số filter từ query hoặc JSON body
$input = json_decode(file_get_contents('php://input'), true);
$search = $conn->real_escape_string($input['search'] ?? ($_GET['search'] ?? ''));
$active = $input['active'] ?? ($_GET['active'] ?? '');

// ✅ Tạo câu SQL
$sql = "SELECT 
            promo_id,
            promo_code,
            promo_type,
            promo_value,
            promo_quantity,
            promo_desc,
            promo_min_order_amount,
            DATE_FORMAT(promo_start_date, '%Y-%m-%d %H:%i:%s') AS promo_start_date,
            DATE_FORMAT(promo_end_date, '%Y-%m-%d %H:%i:%s') AS promo_end_date,
            promo_active
        FROM promotions
        WHERE 1";

// ✅ Thêm điều kiện tìm kiếm nếu có
if (!empty($search)) {
    $searchLike = "%$search%";
    $sql .= " AND (promo_code LIKE '$searchLike' OR promo_desc LIKE '$searchLike')";
}

// ✅ Thêm điều kiện lọc trạng thái (1 = hoạt động, 0 = tắt)
if ($active !== '') {
    $active = intval($active);
    $sql .= " AND promo_active = $active";
}

$sql .= " ORDER BY promo_start_date DESC";

// ✅ Thực thi truy vấn
$result = $conn->query($sql);
$promotions = [];

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $promotions[] = $row;
    }
}

// ✅ Trả phản hồi JSON
http_response_code(200);
echo json_encode([
    'success' => true,
    'count' => count($promotions),
    'promotions' => $promotions
]);

$conn->close();
?>
