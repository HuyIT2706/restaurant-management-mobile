<?php
header('Content-Type: application/json');
require_once __DIR__ . '/vendor/autoload.php';
include('./database.php'); 
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

use Firebase\JWT\JWT;
use Dotenv\Dotenv;
use Firebase\JWT\Key; //thêm dòng này của băng

// load .env
$dotenv = Dotenv::createImmutable(__DIR__);
$dotenv ->safeLoad();
// Key
$secret_key = $_ENV['JWT_SECRET'];

if (empty($secret_key)) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Lỗi cấu hình Server: Thiếu Khóa bí mật JWT!"]);
    exit();
}
// Xử lý đăng nhập
$data = json_decode(file_get_contents('php://input'), true);
$phone = isset($data['phone']) ? $conn->real_escape_string($data['phone']) : '';
$user_password = isset($data['password']) ? $conn->real_escape_string($data['password']) : '';

if (empty($phone) || empty($user_password)) {
    http_response_code(400); 
    echo json_encode(["success" => false, "message" => "Vui lòng nhập đầy đủ thông tin!"]);
    exit();
}

$sql = "SELECT * FROM USERS WHERE user_phone = '$phone' AND user_status = TRUE LIMIT 1";
$result = $conn->query($sql);

if ($result && $result->num_rows === 1) {
    $row = $result->fetch_assoc();

    if (password_verify($user_password, $row['user_password'])) {
        $issued_at = time();
        $expiration_time = $issued_at + (3600 * 8); 

        $payload = array(
            'iat'  => $issued_at,        
            'exp'  => $expiration_time,   
            'data' => array(
                'user_id'   => $row['user_id'],
                'user_role' => $row['user_role'],
                'phone'     => $row['user_phone'],
                'name'      => $row['user_lastname'] . ' ' . $row['user_firstname']
            )
        );

        // 2. Tạo JWT Token
        $jwt = JWT::encode($payload, $secret_key, 'HS256');

        http_response_code(200);
        echo json_encode([
            "success" => true,
            "message" => "Đăng nhập thành công!",
            "token" => $jwt,                 
            "user_id" => $row['user_id'],
            "role" => $row['user_role'],
        ]);
    } else {
        http_response_code(401); 
        echo json_encode(["success" => false, "message" => "Mật khẩu không đúng!"]);
    }
} else {
    http_response_code(401); 
    echo json_encode(["success" => false, "message" => "Số điện thoại không tồn tại hoặc tài khoản bị khóa!"]);
}

$conn->close();
