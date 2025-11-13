<?php
// File: api_promotion_update.php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php');
error_reporting(E_ALL);
ini_set('display_errors', 1);

// ✅ Kiểm tra và xác minh token
$user_data = verifyToken();
if ($user_data->user_role !== 'QuanLy') {
    http_response_code(403);
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền cập nhật khuyến mãi.']);
    exit();
}

// ✅ Hàm chuẩn hóa ngày: bắt đầu = 00:00:00, kết thúc = 23:59:59
function convertToMySQLDate($dateString, $isEndDate = false) {
    if (empty($dateString)) return null;

    // Nếu là định dạng chuẩn ISO hoặc yyyy-MM-dd
    $timestamp = strtotime($dateString);
    if ($timestamp !== false) {
        $date = date('Y-m-d', $timestamp);
        return $isEndDate ? "$date 23:59:59" : "$date 00:00:00";
    }

    // Nếu là dạng dd/MM/yyyy
    if (preg_match('/^(\d{2})\/(\d{2})\/(\d{4})$/', $dateString, $m)) {
        $date = "{$m[3]}-{$m[2]}-{$m[1]}";
        return $isEndDate ? "$date 23:59:59" : "$date 00:00:00";
    }

    return null;
}

// ✅ Cho phép PUT hoặc POST (Retrofit/Compose có thể gửi POST)
$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'PUT' || $method === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    // ✅ Lấy dữ liệu
    $promo_id = filter_var($data['promo_id'] ?? null, FILTER_VALIDATE_INT);
    $promo_code = $conn->real_escape_string($data['promo_code'] ?? '');
    $promo_type = $data['promo_type'] ?? '';
    $promo_value = floatval($data['promo_value'] ?? 0.00);
    $promo_quantity = filter_var($data['promo_quantity'] ?? 0, FILTER_VALIDATE_INT);
    $promo_desc = $conn->real_escape_string($data['promo_desc'] ?? '');
    $promo_min_order_amount = floatval($data['promo_min_order_amount'] ?? 0.00);

    // ✅ Chuyển ngày về đúng định dạng MySQL + cố định giờ
    $promo_start_date = convertToMySQLDate($data['promo_start_date'] ?? '', false) ?? date('Y-m-d 00:00:00');
    $promo_end_date   = convertToMySQLDate($data['promo_end_date'] ?? '', true) ?? date('Y-m-d 23:59:59');

    // Nếu không có ngày kết thúc → cho bằng ngày bắt đầu (23:59:59)
    if (!$promo_end_date) $promo_end_date = str_replace("00:00:00", "23:59:59", $promo_start_date);

    $promo_active = isset($data['promo_active']) ? intval($data['promo_active']) : 1;

    // ✅ Kiểm tra dữ liệu bắt buộc
    if (!$promo_id || empty($promo_code) || empty($promo_type) || $promo_value <= 0) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Vui lòng nhập đầy đủ ID, Mã, Loại và Giá trị hợp lệ!']);
        exit();
    }

    if (!in_array($promo_type, ['PhanTram', 'SoTien'])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Loại khuyến mãi không hợp lệ!']);
        exit();
    }

    // ✅ Cập nhật vào DB
    $sql = "UPDATE promotions 
            SET promo_code=?, promo_type=?, promo_value=?, promo_quantity=?, 
                promo_desc=?, promo_min_order_amount=?, promo_start_date=?, 
                promo_end_date=?, promo_active=?
            WHERE promo_id=?";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param(
        "ssdisdssii",
        $promo_code,
        $promo_type,
        $promo_value,
        $promo_quantity,
        $promo_desc,
        $promo_min_order_amount,
        $promo_start_date,
        $promo_end_date,
        $promo_active,
        $promo_id
    );

    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            http_response_code(200);
            echo json_encode([
                'success' => true,
                'message' => 'Cập nhật khuyến mãi thành công!',
                'promo_id' => $promo_id,
                'promo_start_date' => $promo_start_date,
                'promo_end_date' => $promo_end_date
            ]);
        } else {
            http_response_code(200);
            echo json_encode([
                'success' => true,
                'message' => 'Không có thay đổi dữ liệu.',
                'promo_id' => $promo_id
            ]);
        }
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Lỗi khi cập nhật khuyến mãi: ' . $stmt->error]);
    }

    $stmt->close();
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ.']);
}

$conn->close();
?>