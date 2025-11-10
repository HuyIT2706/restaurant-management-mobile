<?php
// File: api_promotion_update.php
header('Content-Type: application/json');
include('../database.php'); 
include('../auth.php');      
error_reporting(E_ALL);
ini_set('display_errors', 1);

$user_data = verifyToken();
// Check phân quyền
if ($user_data->user_role !== 'QuanLy') {
    http_response_code(403);
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền cập nhật khuyến mãi.']);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    $data = json_decode(file_get_contents('php://input'), true);

    // Lấy dữ liệu và chuẩn bị
    $promo_id = filter_var($data['promo_id'] ?? null, FILTER_VALIDATE_INT);
    $promo_code = $conn->real_escape_string($data['promo_code'] ?? '');
    $promo_type = $data['promo_type'] ?? ''; // Enum: 'PhanTram' or 'SoTien'
    $promo_value = $data['promo_value'] ?? 0.00;
    $promo_quantity = filter_var($data['promo_quantity'] ?? null, FILTER_VALIDATE_INT);
    $promo_desc = $conn->real_escape_string($data['promo_desc'] ?? '');
    $promo_min_order_amount = $data['promo_min_order_amount'] ?? 0.00;
    $promo_start_date = $data['promo_start_date'] ?? ''; // Format: 'YYYY-MM-DD HH:MM:SS'
    $promo_end_date = $data['promo_end_date'] ?? ''; // Format: 'YYYY-MM-DD HH:MM:SS'
    $promo_active = filter_var($data['promo_active'] ?? 1, FILTER_VALIDATE_INT); 

    if (empty($promo_id) || empty($promo_code) || empty($promo_type) || empty($promo_value) || empty($promo_start_date) || empty($promo_end_date)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Thiếu ID, Mã khuyến mãi, Loại, Giá trị, Ngày bắt đầu hoặc Ngày kết thúc.']);
        exit();
    }
    
    // Kiểm tra promo_type hợp lệ
    if (!in_array($promo_type, ['PhanTram', 'SoTien'])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Loại khuyến mãi không hợp lệ!']);
        exit();
    }

    $sql = "UPDATE PROMOTIONS SET promo_code=?, promo_type=?, promo_value=?, promo_quantity=?, promo_desc=?, promo_min_order_amount=?, promo_start_date=?, promo_end_date=?, promo_active=? WHERE promo_id=?";
    $stmt = $conn->prepare($sql);
    
    $stmt->bind_param("ssdisddsii", $promo_code, $promo_type, $promo_value, $promo_quantity, $promo_desc, $promo_min_order_amount, $promo_start_date, $promo_end_date, $promo_active, $promo_id); 

    if ($stmt->execute()) {
        $rows_affected = $stmt->affected_rows;
        
        if ($rows_affected > 0) {
            http_response_code(200); 
            echo json_encode(['success' => true, 'message' => 'Cập nhật khuyến mãi thành công!']);
        } else {
            // Trường hợp dữ liệu không thay đổi
            http_response_code(200); 
            echo json_encode(['success' => true, 'message' => 'Cập nhật thành công (Không có thay đổi dữ liệu).']);
        }
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Lỗi khi cập nhật khuyến mãi vào CSDL: ' . $stmt->error]);
    }
    $stmt->close();
} else {
    http_response_code(405); 
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ.']);
}

$conn->close();
?>