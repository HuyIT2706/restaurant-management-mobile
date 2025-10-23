<?php
include '../database.php';
header('Content-Type: application/json; charset=utf-8');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);

    if (!empty($data)) {
        // Lấy dữ liệu từ body JSON
        $user_id   = intval($data['user_id'] ?? 0);
        $fullname  = trim($data['fullname'] ?? '');
        $phone     = trim($data['phone'] ?? '');
        $gender    = trim($data['gender'] ?? null);
        $role      = trim($data['role'] ?? '');
        $wage      = floatval($data['wage'] ?? 0);
        $status    = intval($data['status'] ?? 1);
        $image     = isset($data['image']) && $data['image'] !== '' ? trim($data['image']) : null;

        // ⚙️ Tách họ và tên (quy tắc: firstname = chữ đầu, lastname = phần còn lại)
        $parts = preg_split('/\s+/', $fullname);
        $firstname = array_shift($parts); // chữ đầu tiên
        $lastname  = implode(' ', $parts); // phần còn lại

        // Kiểm tra dữ liệu bắt buộc
        if ($user_id <= 0 || empty($fullname) || empty($phone) || empty($role)) {
            echo json_encode([
                "status" => "error",
                "message" => "Vui lòng nhập đầy đủ thông tin bắt buộc!"
            ]);
            exit;
        }

        // Kiểm tra xem nhân viên có tồn tại chưa
        $check = $conn->prepare("SELECT user_id FROM users WHERE user_id = ?");
        $check->bind_param("i", $user_id);
        $check->execute();
        $result = $check->get_result();

        if ($result->num_rows === 0) {
            echo json_encode([
                "status" => "error",
                "message" => "Không tìm thấy nhân viên cần cập nhật!"
            ]);
            exit;
        }

        // Kiểm tra số điện thoại có bị trùng với người khác không
        $checkPhone = $conn->prepare("SELECT user_id FROM users WHERE user_phone = ? AND user_id != ?");
        $checkPhone->bind_param("si", $phone, $user_id);
        $checkPhone->execute();
        $resPhone = $checkPhone->get_result();

        if ($resPhone->num_rows > 0) {
            echo json_encode([
                "status" => "error",
                "message" => "Số điện thoại đã được sử dụng bởi nhân viên khác!"
            ]);
            exit;
        }

        // ✅ Cập nhật thông tin nhân viên
        $sql = "UPDATE users SET 
                    user_firstname = ?, 
                    user_lastname = ?, 
                    user_phone = ?, 
                    user_gender = ?, 
                    user_wage = ?, 
                    user_image = ?, 
                    user_role = ?, 
                    user_status = ?, 
                    user_updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ?";

        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ssssdssii", 
            $firstname, 
            $lastname, 
            $phone, 
            $gender, 
            $wage, 
            $image, 
            $role, 
            $status, 
            $user_id
        );

        if ($stmt->execute()) {
            echo json_encode([
                "status" => "success",
                "message" => "Cập nhật thông tin nhân viên thành công!",
                "user_id" => $user_id,
                "user_firstname" => $firstname,
                "user_lastname" => $lastname
            ]);
        } else {
            echo json_encode([
                "status" => "error",
                "message" => "Lỗi khi cập nhật: " . $stmt->error
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
