<?php
include '../database.php';
include '../auth.php';
$user_data = verifyToken(); // Kiểm tra token
if ($user_data->user_role !== 'QuanLy') {
    echo json_encode(["success" => false, "message" => "Bạn không có quyền truy cập!"]);
    exit;
}

header('Content-Type: application/json; charset=utf-8');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);

    if (!empty($data)) {
        // Nhận dữ liệu từ body JSON
        $fullname   = trim($data['fullname'] ?? '');
        $phone      = trim($data['phone'] ?? '');
        $gender     = trim($data['gender'] ?? null);
        $role       = trim($data['role'] ?? '');
        $wage       = floatval($data['wage'] ?? 0);
        $status     = intval($data['status'] ?? 1);
        $image      = isset($data['image']) && $data['image'] !== '' ? trim($data['image']) : null;
        $password   = password_hash("123456", PASSWORD_BCRYPT); // mật khẩu mặc định

        // ⚙️ TÁCH HỌ & TÊN (theo quy tắc bạn yêu cầu)
        $parts = preg_split('/\s+/', $fullname);
        $firstname = array_shift($parts); // chữ đầu tiên
        $lastname = implode(' ', $parts); // phần còn lại

        // Kiểm tra dữ liệu bắt buộc
        if (empty($fullname) || empty($phone) || empty($role)) {
            echo json_encode([
                "status" => "error",
                "message" => "Vui lòng nhập đầy đủ thông tin bắt buộc!"
            ]);
            exit;
        }

        // Kiểm tra trùng số điện thoại
        $check = $conn->prepare("SELECT user_id FROM users WHERE user_phone = ?");
        $check->bind_param("s", $phone);
        $check->execute();
        $result = $check->get_result();

        if ($result->num_rows > 0) {
            echo json_encode([
                "status" => "error",
                "message" => "Số điện thoại này đã tồn tại trong hệ thống!"
            ]);
            exit;
        }

        // Thêm nhân viên mới
        $sql = "INSERT INTO users 
                (user_phone, user_password, user_firstname, user_lastname, user_gender, user_wage, user_image, user_role, user_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        $stmt = $conn->prepare($sql);
        $stmt->bind_param("sssssdssi", $phone, $password, $firstname, $lastname, $gender, $wage, $image, $role, $status);

        if ($stmt->execute()) {
            echo json_encode([
                "status" => "success",
                "message" => "Thêm nhân viên mới thành công!",
                "user_id" => $stmt->insert_id,
                "user_firstname" => $firstname,
                "user_lastname" => $lastname
            ]);
        } else {
            echo json_encode([
                "status" => "error",
                "message" => "Lỗi khi thêm nhân viên: " . $stmt->error
            ]);
        }

        $stmt->close();
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Không có dữ liệu gửi lên!"
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
