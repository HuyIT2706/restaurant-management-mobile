<?php
header('Content-Type: application/json');
include('../database.php'); 
include('../auth.php');      
error_reporting(E_ALL);
ini_set('display_errors', 1);


$user_data = verifyToken();
//  Check phân quyền
if ($user_data->user_role !== 'QuanLy') {
    http_response_code(403);
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền cập nhật sản phẩm.']);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    $data = json_decode(file_get_contents('php://input'), true);

    // Lấy dữ liệu và chuẩn bị
    $product_id = filter_var($data['product_id'] ?? null, FILTER_VALIDATE_INT);
    $name = $conn->real_escape_string($data['name'] ?? '');
    $description = $conn->real_escape_string($data['description'] ?? '');
    $category_name = $conn->real_escape_string($data['category'] ?? ''); 
    $price = $data['price'] ?? 0.00;
    $image_url = $conn->real_escape_string($data['image_url'] ?? '');
    $product_active = filter_var($data['product_active'] ?? 1, FILTER_VALIDATE_INT); 

    if (empty($product_id) || empty($name) || empty($category_name) || empty($price)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Thiếu ID, Tên, Loại món, hoặc Giá.']);
        exit();
    }
    
    $sql_category = "SELECT category_id FROM CATEGORIES WHERE category_name = ?";
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

    $sql = "UPDATE PRODUCTS SET product_name=?, product_description=?, category_id=?, price=?, image_url=?, product_active=? WHERE product_id=?";
    $stmt = $conn->prepare($sql);
    
    $stmt->bind_param("ssidsii", $name, $description, $category_id, $price, $image_url, $product_active, $product_id); 

    if ($stmt->execute()) {
        $rows_affected = $stmt->affected_rows;
        
        if ($rows_affected > 0) {
            http_response_code(200); 
            echo json_encode(['success' => true, 'message' => 'Cập nhật sản phẩm thành công!']);
        } else {
            // Trường hợp dữ liệu không thay đổi
            http_response_code(200); 
            echo json_encode(['success' => true, 'message' => 'Cập nhật thành công (Không có thay đổi dữ liệu).']);
        }
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Lỗi khi cập nhật sản phẩm vào CSDL: ' . $stmt->error]);
    }
    $stmt->close();
} else {
    http_response_code(405); 
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ.']);
}

$conn->close();
?>