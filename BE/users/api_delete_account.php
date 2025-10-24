<?php
include '../database.php';
include '../auth.php';
$user_data = verifyToken(); // Kiểm tra token
if ($user_data->user_role !== 'QuanLy') {
    echo json_encode(["success" => false, "message" => "Bạn không có quyền truy cập!"]);
    exit;
}

header('Content-Type: application/json; charset=utf-8');

// Chỉ chấp nhận phương thức POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    // Lấy dữ liệu từ JSON gửi lên
    $data = json_decode(file_get_contents("php://input"), true);
    $user_id = isset($data['user_id']) ? intval($data['user_id']) : 0;

    if ($user_id > 0) {
        // Kiểm tra xem nhân viên có tồn tại không
        $check = $conn->prepare("SELECT * FROM users WHERE user_id = ?");
        $check->bind_param("i", $user_id);
        $check->execute();
        $result = $check->get_result();

        if ($result->num_rows === 0) {
            echo json_encode([
                "status" => "error",
                "message" => "Không tìm thấy nhân viên cần xoá!"
            ]);
            exit;
        }

        // Xoá nhân viên
        $delete = $conn->prepare("DELETE FROM users WHERE user_id = ?");
        $delete->bind_param("i", $user_id);

        if ($delete->execute()) {
            echo json_encode([
                "status" => "success",
                "message" => "Xoá nhân viên thành công!"
            ]);
        } else {
            echo json_encode([
                "status" => "error",
                "message" => "Lỗi khi xoá nhân viên: " . $delete->error
            ]);
        }

        $delete->close();
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Thiếu hoặc sai user_id!"
        ]);
    }
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Phương thức không hợp lệ!"
    ]);
}

$conn->close();
?>
