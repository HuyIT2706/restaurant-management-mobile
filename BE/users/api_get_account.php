<?php
include '../database.php';
header('Content-Type: application/json; charset=utf-8');

// Chỉ cho phép phương thức GET hoặc POST (tùy app bạn gọi)
if ($_SERVER['REQUEST_METHOD'] === 'GET' || $_SERVER['REQUEST_METHOD'] === 'POST') {

    // Truy vấn tất cả nhân viên
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
            ORDER BY user_id DESC";

    $result = $conn->query($sql);

    if ($result && $result->num_rows > 0) {
        $accounts = [];

        while ($row = $result->fetch_assoc()) {
            $accounts[] = [
                "user_id" => intval($row['user_id']),
                "fullname" => trim($row['user_firstname'] . ' ' . $row['user_lastname']),
                "gender" => $row['user_gender'],
                "phone" => $row['user_phone'],
                "role" => $row['user_role'],
                "wage" => floatval($row['user_wage']),
                "status" => intval($row['user_status']),
                "image" => $row['user_image'],
                "created_at" => $row['user_created_at'],
                "updated_at" => $row['user_updated_at']
            ];
        }

        echo json_encode([
            "status" => "success",
            "total" => count($accounts),
            "data" => $accounts
        ]);
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Không tìm thấy nhân viên nào!"
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
