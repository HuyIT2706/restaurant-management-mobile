<?php
include '../database.php';
header('Content-Type: application/json; charset=utf-8');

$data = json_decode(file_get_contents('php://input'), true);

if (!$data) {
    echo json_encode(['success' => false, 'message' => 'Không nhận được dữ liệu từ client']);
    exit();
}

if (!isset($data['table_id'])) {
    echo json_encode(['success' => false, 'message' => 'Thiếu mã bàn!']);
    exit();
}

$table_id = intval($data['table_id']);

// Kiểm tra xem bàn có tồn tại không
$checkSql = "SELECT table_id FROM tables WHERE table_id = ?";
$checkStmt = $conn->prepare($checkSql);
$checkStmt->bind_param("i", $table_id);
$checkStmt->execute();
$checkStmt->store_result();

if ($checkStmt->num_rows === 0) {
    echo json_encode(['success' => false, 'message' => 'Không tìm thấy bàn này!']);
    exit();
}

// Cập nhật trạng thái bàn — ví dụ: “Đang phục vụ”
$updateSql = "UPDATE tables SET status = 'Đang phục vụ' WHERE table_id = ?";
$updateStmt = $conn->prepare($updateSql);
$updateStmt->bind_param("i", $table_id);

if ($updateStmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Chọn bàn thành công!']);
} else {
    echo json_encode(['success' => false, 'message' => 'Không thể chọn bàn!']);
}

?>
