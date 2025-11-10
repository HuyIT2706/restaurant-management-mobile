<?php
include '../database.php';
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

$data = json_decode(file_get_contents('php://input'), true);

if (!$data || !isset($data['table_id'])) {
    echo json_encode(['success' => false, 'message' => 'Thiếu mã bàn']);
    exit;
}

$table_id = intval($data['table_id']);

// Kiểm tra bàn có tồn tại không
$checkSql = "SELECT status FROM tables WHERE table_id = ?";
$checkStmt = $conn->prepare($checkSql);
$checkStmt->bind_param("i", $table_id);
$checkStmt->execute();
$checkStmt->store_result();

if ($checkStmt->num_rows === 0) {
    echo json_encode(['success' => false, 'message' => 'Không tìm thấy bàn này!']);
    exit();
}

$checkStmt->bind_result($currentStatus);
$checkStmt->fetch();

// 🔧 Đổi trạng thái theo đúng DB của bạn
$newStatus = ($currentStatus === 'Trong') ? 'Dang phuc vu' : 'Trong';

// Cập nhật trạng thái bàn
$updateSql = "UPDATE tables SET status = ? WHERE table_id = ?";
$updateStmt = $conn->prepare($updateSql);
$updateStmt->bind_param("si", $newStatus, $table_id);

if ($updateStmt->execute()) {
    echo json_encode([
        'success' => true,
        'message' => ($newStatus === 'Dang phuc vu') ? 'Bàn đã được chọn!' : 'Bàn đã được bỏ chọn!',
        'table_id' => $table_id,
        'new_status' => $newStatus
    ], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(['success' => false, 'message' => 'Không thể cập nhật trạng thái bàn!']);
}

$conn->close();
?>
