<?php
// File: api_promotion_add.php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php'); 
error_reporting(E_ALL);
ini_set('display_errors', 1);

$secret_key = $_ENV['JWT_SECRET'];

// 1. CHẠY MIDDLEWARE XÁC MINH TOKEN VÀ PHÂN QUYỀN
$user_data = verifyToken();

// Kiểm tra quyền: Chỉ cho phép Quản lý (QuanLy) thêm khuyến mãi
if ($user_data->user_role !== 'QuanLy') {
    http_response_code(403); // Forbidden
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền thêm khuyến mãi.']);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Nhận dữ liệu từ Frontend (dạng JSON)
    $data = json_decode(file_get_contents('php://input'), true);

    // Lấy dữ liệu và chuẩn bị
    $promo_code = $conn->real_escape_string($data['promo_code'] ?? '');
    $promo_type = $data['promo_type'] ?? ''; // Enum: 'PhanTram' or 'SoTien'
    $promo_value = $data['promo_value'] ?? 0.00;
    $promo_quantity = filter_var($data['promo_quantity'] ?? null, FILTER_VALIDATE_INT);
    $promo_desc = $conn->real_escape_string($data['promo_desc'] ?? '');
    $promo_min_order_amount = $data['promo_min_order_amount'] ?? 0.00;
    $promo_start_date = $data['promo_start_date'] ?? ''; // Format: 'YYYY-MM-DD HH:MM:SS'
    $promo_end_date = $data['promo_end_date'] ?? ''; // Format: 'YYYY-MM-DD HH:MM:SS'
    
    // Trạng thái: Mặc định là TRUE (1) cho khuyến mãi mới
    $promo_active = 1;

    if (empty($promo_code) || empty($promo_type) || empty($promo_value) || empty($promo_start_date) || empty($promo_end_date)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Vui lòng nhập đầy đủ Mã khuyến mãi, Loại, Giá trị, Ngày bắt đầu và Ngày kết thúc!']);
        exit();
    }

    // Kiểm tra promo_type hợp lệ
    if (!in_array($promo_type, ['PhanTram', 'SoTien'])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Loại khuyến mãi không hợp lệ!']);
        exit();
    }

    $sql = "INSERT INTO PROMOTIONS (promo_code, promo_type, promo_value, promo_quantity, promo_desc, promo_min_order_amount, promo_start_date, promo_end_date, promo_active) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    
    $stmt->bind_param("ssdisddsi", $promo_code, $promo_type, $promo_value, $promo_quantity, $promo_desc, $promo_min_order_amount, $promo_start_date, $promo_end_date, $promo_active); 

    if ($stmt->execute()) {
        http_response_code(201); 
        echo json_encode([
            'success' => true, 
            'message' => 'Thêm khuyến mãi thành công!',
            'promo_id' => $conn->insert_id
        ]);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Lỗi khi thêm khuyến mãi vào CSDL: ' . $stmt->error]);
    }
    $stmt->close();
} else {
    http_response_code(405); 
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ.']);
}

$conn->close();
?>