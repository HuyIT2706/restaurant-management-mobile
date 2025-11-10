<?php
include '../database.php';
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

$sql = "SELECT table_id, table_name, status FROM tables ORDER BY table_id ASC";
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
    $tables[] = [
        'id' => (int)$row['table_id'],
        'name' => $row['table_name'],
        'status' => $row['status']
    ];
}

echo json_encode([
    'success' => true,
    'count' => count($tables),
    'data' => $tables
], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);

$conn->close();
?>
