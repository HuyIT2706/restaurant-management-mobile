<?php
include '../database.php'; // 🔧 đường dẫn đúng
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

// ✅ Chỉ cho phép POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Phương thức không hợp lệ (chỉ chấp nhận POST)'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// ✅ Lấy dữ liệu từ client
$data = json_decode(file_get_contents('php://input'), true);

if (!$data || !isset($data['table_id']) || !isset($data['status'])) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Thiếu dữ liệu đầu vào (table_id, status)'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$table_id = intval($data['table_id']);
$status = trim($data['status']);

// ✅ Các trạng thái hợp lệ trong DB của bạn
$valid_status = ['Trong', 'Dang phuc vu'];
if (!in_array($status, $valid_status)) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Trạng thái không hợp lệ. Chỉ chấp nhận: "Trong", "Dang phuc vu".'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// ✅ Kiểm tra xem bàn có tồn tại không
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

// ✅ Cập nhật trạng thái bàn
$update_sql = "UPDATE tables SET status = ? WHERE table_id = ?";
$update_stmt = $conn->prepare($update_sql);
$update_stmt->bind_param("si", $status, $table_id);

if ($update_stmt->execute()) {
    echo json_encode([
        'success' => true,
        'message' => "Cập nhật trạng thái bàn thành công.",
        'data' => [
            'table_id' => $table_id,
            'new_status' => $status
        ]
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
} else {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi khi cập nhật trạng thái bàn: ' . $conn->error
    ], JSON_UNESCAPED_UNICODE);
}

$update_stmt->close();
$conn->close();
?>
