<?php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php');
error_reporting(E_ALL);
ini_set('display_errors', 1);

// ✅ Kiểm tra token & quyền
$user_data = verifyToken();
if ($user_data->user_role !== 'QuanLy') {
    http_response_code(403);
    echo json_encode([
        'success' => false,
        'message' => 'Bạn không có quyền xóa khuyến mãi.'
    ]);
    exit();
}

// ✅ Cho phép cả DELETE và POST (vì client có thể gửi POST)
$method = $_SERVER['REQUEST_METHOD'];
if ($method === 'DELETE' || $method === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    // ✅ Lấy ID khuyến mãi (ưu tiên theo thứ tự: body → POST → GET)
    $promo_id = $data['promo_id'] ?? ($_POST['promo_id'] ?? ($_GET['id'] ?? null));
    $promo_id = filter_var($promo_id, FILTER_VALIDATE_INT);

    if (empty($promo_id)) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Vui lòng cung cấp ID khuyến mãi hợp lệ!'
        ]);
        exit();
    }

    // ✅ Kiểm tra khuyến mãi tồn tại
    $check_sql = "SELECT promo_id FROM promotions WHERE promo_id = ?";
    $check_stmt = $conn->prepare($check_sql);
    $check_stmt->bind_param("i", $promo_id);
    $check_stmt->execute();
    $check_result = $check_stmt->get_result();

    if ($check_result->num_rows === 0) {
        http_response_code(404);
        echo json_encode([
            'success' => false,
            'message' => "Không tìm thấy khuyến mãi có ID = $promo_id!"
        ]);
        $check_stmt->close();
        exit();
    }
    $check_stmt->close();

    // ✅ Tiến hành xóa
    $sql = "DELETE FROM promotions WHERE promo_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $promo_id);

    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            http_response_code(200);
            echo json_encode([
                'success' => true,
                'message' => 'Xóa khuyến mãi thành công!',
                'promo_id' => $promo_id
            ]);
        } else {
            http_response_code(200);
            echo json_encode([
                'success' => true,
                'message' => 'Không có khuyến mãi nào bị xóa (có thể đã bị xóa trước đó).',
                'promo_id' => $promo_id
            ]);
        }
    } else {
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'message' => 'Lỗi khi xóa khuyến mãi: ' . $stmt->error
        ]);
    }

    $stmt->close();
} else {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Phương thức không được hỗ trợ. Vui lòng dùng DELETE hoặc POST.'
    ]);
}

$conn->close();
?>