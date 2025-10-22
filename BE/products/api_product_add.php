<?php
include '../database.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (!$data) {
    echo json_encode(['success' => false, 'message' => 'Không nhận được dữ liệu từ client']);
    exit();
}

$phone = $data['phone'];
$password = password_hash($data['password'], PASSWORD_DEFAULT);
$firstname = $data['firstname'];
$lastname = $data['lastname'];
$gender = $data['gender'];
$wage = $data['wage'];

// Kiểm tra trùng SĐT
$checkSql = "SELECT user_id FROM users WHERE user_phone = ?";
$checkStmt = $conn->prepare($checkSql);
$checkStmt->bind_param("s", $phone);
$checkStmt->execute();
$checkStmt->store_result();

if ($checkStmt->num_rows > 0) {
    echo json_encode(['success' => false, 'message' => 'SĐT đã có trong danh sách người dùng!']);
    exit();
}

// Thêm user mới
$sql = "INSERT INTO users (user_phone, user_password, user_firstname, user_lastname, user_gender, user_wage)
        VALUES (?, ?, ?, ?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("sssssd", $phone, $password, $firstname, $lastname, $gender, $wage);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Thêm người dùng thành công!']);
} else {
    echo json_encode(['success' => false, 'message' => 'Thêm thất bại!']);
}

?>