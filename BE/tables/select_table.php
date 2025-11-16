<?php
include '../database.php';
include '../auth.php';
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

$user_data = verifyToken();
if ($user_data->user_role !== 'Order' && $user_data->user_role !== 'QuanLy') {
    http_response_code(403);
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền chọn bàn.'], JSON_UNESCAPED_UNICODE);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ. Vui lòng dùng POST.'], JSON_UNESCAPED_UNICODE);
    exit();
}

$data = json_decode(file_get_contents('php://input'), true);

if (!$data || !isset($data['table_id'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Thiếu mã bàn'], JSON_UNESCAPED_UNICODE);
    exit;
}

$table_id = intval($data['table_id']);

$checkSql = "SELECT status FROM tables WHERE table_id = ?";
$checkStmt = $conn->prepare($checkSql);
$checkStmt->bind_param("i", $table_id);
$checkStmt->execute();
$checkStmt->store_result();

if ($checkStmt->num_rows === 0) {
    http_response_code(404);
    echo json_encode(['success' => false, 'message' => 'Không tìm thấy bàn này!'], JSON_UNESCAPED_UNICODE);
    $checkStmt->close();
    $conn->close();
    exit();
}

$checkStmt->bind_result($currentStatus);
$checkStmt->fetch();
$checkStmt->close();

$newStatus = ($currentStatus === 'Trong') ? 'Dang phuc vu' : 'Trong';

$updateSql = "UPDATE tables SET status = ? WHERE table_id = ?";
$updateStmt = $conn->prepare($updateSql);
$updateStmt->bind_param("si", $newStatus, $table_id);

if ($updateStmt->execute()) {
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => ($newStatus === 'Dang phuc vu') ? 'Bàn đã được chọn!' : 'Bàn đã được bỏ chọn!',
        'table_id' => $table_id,
        'new_status' => $newStatus
    ], JSON_UNESCAPED_UNICODE);
} else {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Không thể cập nhật trạng thái bàn!'], JSON_UNESCAPED_UNICODE);
}

$updateStmt->close();
$conn->close();
?>
