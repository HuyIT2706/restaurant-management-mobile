<?php

use Firebase\JWT\JWT;
use Firebase\JWT\Key;

// Giả định .env đã được load và chứa JWT_SECRET
$secret_key = $_ENV['JWT_SECRET'] ?? null;
$algorithm = 'HS256';

function verifyToken()
{
    global $secret_key, $algorithm; // <-- Đảm bảo truy cập biến toàn cục

    // Kiểm tra Khóa: Nếu lỗi, chứng tỏ .env chưa load thành công
    if (empty($secret_key)) {
        http_response_code(500);
        // Thông báo lỗi rõ ràng hơn cho người dùng
        echo json_encode(["success" => false, "message" => "Lỗi cấu hình: Khóa bí mật JWT chưa được thiết lập."]);
        exit();
    }

    $headers = getallheaders();
    $token = null;

    // 1. Kiểm tra header Authorization: Bearer <token>
    if (isset($headers['Authorization']) && preg_match('/Bearer\s(\S+)/', $headers['Authorization'], $matches)) {
        $token = $matches[1];
    }

    // Kiểm tra biến môi trường HTTP_AUTHORIZATION (đôi khi server dùng cái này)
    elseif (isset($_SERVER['HTTP_AUTHORIZATION']) && preg_match('/Bearer\s(\S+)/', $_SERVER['HTTP_AUTHORIZATION'], $matches)) {
        $token = $matches[1];
    }

    // Nếu không tìm thấy Token
    if (!$token) {
        http_response_code(401); // Unauthorized
        echo json_encode(["success" => false, "message" => "Yêu cầu truy cập bị từ chối: Thiếu Token (Authorization Header)."]);
        exit();
    }

    try {
        // 2. Giải mã Token
        // Key là Khóa bí mật, Algorithm là thuật toán mã hóa
        $decoded = JWT::decode($token, new Key($secret_key, $algorithm));

        // 3. Trả về dữ liệu Payload đã giải mã (chứa user_id, user_role)
        return $decoded->data;
    } catch (\Firebase\JWT\ExpiredException $e) {
        http_response_code(401); // Unauthorized (Token hết hạn)
        echo json_encode(["success" => false, "message" => "Token đã hết hạn. Vui lòng đăng nhập lại."]);
        exit();
    } catch (Exception $e) {
        http_response_code(401); // Unauthorized (Token không hợp lệ)
        echo json_encode(["success" => false, "message" => "Token không hợp lệ. Lỗi: " . $e->getMessage()]);
        exit();
    }
}
