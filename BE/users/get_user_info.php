<?php
include '../database.php';
header('Content-Type: application/json; charset=utf-8');

// Chỉ chấp nhận phương thức POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);

    $user_id = intval($data['user_id'] ?? 0);

    if ($user_id <= 0) {
        echo json_encode([
            "status" => "error",
            "message" => "Thiếu hoặc sai user_id!"
        ]);
        exit;
    }

    // Lấy thông tin chi tiết nhân viên theo user_id
    $sql = "SELECT 
                user_id,
                user_firstname,
                user_lastname,
                user_gender,
                user_phone,
                user_role,
                user_wage,
                user_status,
                user_image,
                user_created_at,
                user_updated_at
            FROM users
            WHERE user_id = ?";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result && $result->num_rows > 0) {
        $user = $result->fetch_assoc();

        // Gộp họ + tên thành fullname
        $user['fullname'] = trim($user['user_firstname'] . ' ' . $user['user_lastname']);

        echo json_encode([
            "status" => "success",
            "data" => [
                "user_id" => intval($user['user_id']),
                "fullname" => $user['fullname'],
                "firstname" => $user['user_firstname'],
                "lastname" => $user['user_lastname'],
                "gender" => $user['user_gender'],
                "phone" => $user['user_phone'],
                "role" => $user['user_role'],
                "wage" => floatval($user['user_wage']),
                "status" => intval($user['user_status']),
                "image" => $user['user_image'],
                "created_at" => $user['user_created_at'],
                "updated_at" => $user['user_updated_at']
            ]
        ]);
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Không tìm thấy nhân viên!"
        ]);
    }

    $stmt->close();
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Phương thức không hợp lệ!"
    ]);
}

$conn->close();
?>
