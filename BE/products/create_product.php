<?php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php'); 
error_reporting(E_ALL);
ini_set('display_errors', 1);

$secret_key = $_ENV['JWT_SECRET'];

// 1. CHẠY MIDDLEWARE XÁC MINH TOKEN VÀ PHÂN QUYỀN
$user_data = verifyToken();

// Kiểm tra quyền: Chỉ cho phép Quản lý (QuanLy) thêm sản phẩm
if ($user_data->user_role !== 'QuanLy') {
    http_response_code(403); // Forbidden
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền thêm sản phẩm.']);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Nhận dữ liệu từ Frontend (dạng JSON)
    $data = json_decode(file_get_contents('php://input'), true);

    // Lấy dữ liệu và chuẩn bị
    $name = $conn->real_escape_string($data['name'] ?? '');
    $description = $conn->real_escape_string($data['description'] ?? '');
    $category_name = $conn->real_escape_string($data['category'] ?? ''); 
    $price = $data['price'] ?? 0.00;
    $image_url = $conn->real_escape_string($data['image_url'] ?? '');
    
    // Trạng thái: Mặc định là TRUE (1) cho sản phẩm mới
    $product_active = 1;

    if (empty($name) || empty($category_name) || empty($price)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Vui lòng nhập đầy đủ Tên món, Loại món và Giá!']);
        exit();
    }
    
    
    $sql_category = "SELECT category_id FROM categories WHERE category_name = ?";
    $stmt_cat = $conn->prepare($sql_category);
    $stmt_cat->bind_param("s", $category_name);
    $stmt_cat->execute();
    $result_cat = $stmt_cat->get_result();
    $category_row = $result_cat->fetch_assoc();
    $stmt_cat->close();

    if (!$category_row) {
        http_response_code(404);
        echo json_encode(['success' => false, 'message' => "Loại món '$category_name' không tồn tại!"]);
        exit();
    }
    
    $category_id = $category_row['category_id'];

    $sql = "INSERT INTO products (product_name, product_description, category_id, price, image_url, product_active) VALUES (?, ?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    
    $stmt->bind_param("ssidsi", $name, $description, $category_id, $price, $image_url, $product_active); 

    if ($stmt->execute()) {
        http_response_code(201); 
        echo json_encode([
            'success' => true, 
            'message' => 'Thêm sản phẩm thành công!',
            'product_id' => $conn->insert_id
        ]);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Lỗi khi thêm sản phẩm vào CSDL: ' . $stmt->error]);
    }
    $stmt->close();
} else {
    http_response_code(405); 
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ.']);
}

$conn->close();
?>