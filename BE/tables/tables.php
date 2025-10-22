<?php
include '../database.php';
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

// Lấy danh sách tất cả các bàn
$sql = "SELECT table_id AS id, table_name AS name, status 
        FROM tables 
        ORDER BY table_id ASC";

$result = $conn->query($sql);

if (!$result) {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi truy vấn: ' . $conn->error
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

$tables = [];
while ($row = $result->fetch_assoc()) {
    $tables[] = $row;
}

// Trả kết quả JSON chuẩn hóa
if (count($tables) > 0) {
    echo json_encode([
        'success' => true,
        'count' => count($tables),
        'data' => $tables
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Không có bàn nào trong danh sách!'
    ], JSON_UNESCAPED_UNICODE);
}

$conn->close();
?>
