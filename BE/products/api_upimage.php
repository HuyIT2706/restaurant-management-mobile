<?php
header('Content-Type: application/json');
include('../database.php'); 
include('../auth.php');     
error_reporting(E_ALL);
ini_set('display_errors', 1);

$user_data = verifyToken();

if ($user_data->user_role !== 'QuanLy') {
    http_response_code(403);
    echo json_encode(["success" => false, "message" => "Bạn không có quyền tải ảnh lên hệ thống."]);
    exit();
}
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    
    if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "Không có tệp ảnh hoặc có lỗi khi tải lên."]);
        exit();
    }

    $file = $_FILES['image'];
    
    $upload_dir = dirname(__DIR__) . '/assets/products/';
    
    if (!is_dir($upload_dir)) {
        mkdir($upload_dir, 0777, true);
    }

    $file_extension = pathinfo($file['name'], PATHINFO_EXTENSION);
    $new_file_name = uniqid('prod_', true) . '.' . strtolower($file_extension);
    $target_path = $upload_dir . $new_file_name;

    if (move_uploaded_file($file['tmp_name'], $target_path)) {
        $base_url = "http://localhost/BeMobile/BE/assets/products/"; 
        $public_url = $base_url . $new_file_name;
        
        http_response_code(200);
        echo json_encode([
            "success" => true,
            "message" => "Tải ảnh lên thành công.",
            "image_url" => $public_url, 
            "file_name" => $new_file_name
        ]);

    } else {
        http_response_code(500);
        echo json_encode(["success" => false, "message" => "Không thể di chuyển tệp vào thư mục đích. (Kiểm tra quyền ghi của thư mục)"]);
    }
} else {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Phương thức không được hỗ trợ. Vui lòng dùng POST."]);
}
?>