<?php
include '../database.php'; // 🔧 Đảm bảo đường dẫn chính xác
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

// ✅ Chỉ chấp nhận POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Phương thức không hợp lệ (chỉ chấp nhận POST)'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// ✅ Đọc dữ liệu từ client
$data = json_decode(file_get_contents('php://input'), true);

if (!$data || !isset($data['table_id'])) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Thiếu mã bàn (table_id)'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$table_id = intval($data['table_id']);
$table_name = isset($data['table_name']) ? trim($data['table_name']) : null;
$status = isset($data['status']) ? trim($data['status']) : null;

// ✅ Kiểm tra bàn có tồn tại không
$check_sql = "SELECT table_id FROM tables WHERE table_id = ?";
$check_stmt = $conn->prepare($check_sql);
$check_stmt->bind_param("i", $table_id);
$check_stmt->execute();
$check_stmt->store_result();

if ($check_stmt->num_rows === 0) {
    http_response_code(404);
    echo json_encode([
        'success' => false,
        'message' => 'Không tìm thấy bàn có mã này.'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// ✅ Xây dựng câu lệnh UPDATE động
$update_fields = [];
$params = [];
$types = "";

if ($table_name !== null) {
    $update_fields[] = "table_name = ?";
    $params[] = $table_name;
    $types .= "s";
}

if ($status !== null) {
    $update_fields[] = "status = ?";
    $params[] = $status;
    $types .= "s";
}

if (empty($update_fields)) {
    echo json_encode([
        'success' => false,
        'message' => 'Không có dữ liệu nào để cập nhật.'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$sql = "UPDATE tables SET " . implode(", ", $update_fields) . " WHERE table_id = ?";
$params[] = $table_id;
$types .= "i";

$stmt = $conn->prepare($sql);
$stmt->bind_param($types, ...$params);

// ✅ Thực thi cập nhật
if ($stmt->execute()) {
    echo json_encode([
        'success' => true,
        'message' => 'Cập nhật thông tin bàn thành công.',
        'data' => [
            'table_id' => $table_id,
            'table_name' => $table_name,
            'status' => $status
        ]
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
} else {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi khi cập nhật bàn: ' . $conn->error
    ], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>
