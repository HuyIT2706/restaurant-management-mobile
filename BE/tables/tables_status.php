<?php
include '../database.php';
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

// Chỉ cho phép POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Chỉ chấp nhận phương thức POST']);
    exit();
}

// Đọc JSON từ client
$data = json_decode(file_get_contents('php://input'), true);

if (!$data || !isset($data['table_id']) || !isset($data['status'])) {
    echo json_encode(['success' => false, 'message' => 'Thiếu dữ liệu table_id hoặc status']);
    exit();
}

$table_id = intval($data['table_id']);
$status = trim($data['status']);

// Xác định giá trị hợp lệ cho status
$valid_status = ['available', 'occupied', 'reserved'];
if (!in_array($status, $valid_status)) {
    echo json_encode(['success' => false, 'message' => 'Giá trị status không hợp lệ']);
    exit();
}

// Kiểm tra bàn có tồn tại không
$checkSql = "SELECT table_id FROM tables WHERE table_id = ?";
$checkStmt = $conn->prepare($checkSql);
$checkStmt->bind_param("i", $table_id);
$checkStmt->execute();
$checkStmt->store_result();

if ($checkStmt->num_rows === 0) {
    echo json_encode(['success' => false, 'message' => 'Không tìm thấy bàn với mã này']);
    exit();
}

// Cập nhật trạng thái bàn
$updateSql = "UPDATE tables SET status = ? WHERE table_id = ?";
$updateStmt = $conn->prepare($updateSql);
$updateStmt->bind_param("si", $status, $table_id);

if ($updateStmt->execute()) {
    echo json_encode([
        'success' => true,
        'message' => 'Cập nhật trạng thái bàn thành công!',
        'table_id' => $table_id,
        'new_status' => $status
    ], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(['success' => false, 'message' => 'Cập nhật thất bại!']);
}

$updateStmt->close();
$conn->close();
?>
