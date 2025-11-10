<?php
// File: api_promotion_delete.php
header("Content-Type: application/json");
include("../database.php");
include("../auth.php");
error_reporting(E_ALL);
ini_set("display_errors", 1);

$userData = verifyToken();

// Kiểm tra token, phân quyền
if ($userData->user_role !== "QuanLy") {
    http_response_code(403);
    echo json_encode(["success" => false, "message" => "Bạn không đủ thẩm quyền để thực hiện chức năng này!"]);
    exit();
}

if ($_SERVER["REQUEST_METHOD"] === "DELETE") {
    $promo_id = filter_var($_GET["id"] ?? null, FILTER_VALIDATE_INT);
    
    if (empty($promo_id)) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "Vui lòng cung cấp ID khuyến mãi hợp lệ"]);
        exit();
    }

    $sql = "DELETE FROM PROMOTIONS WHERE promo_id = ?";
    $stmt = $conn->prepare($sql); 
    $stmt->bind_param("i", $promo_id);
    
    if ($stmt->execute()) {
        $rowDelete = $stmt->affected_rows;
        
        if ($rowDelete > 0) {
            http_response_code(200);
            echo json_encode(["success" => true, "message" => "Xóa khuyến mãi thành công"]);
        } else {
            http_response_code(404);
            echo json_encode(["success" => false, "message" => "Không tìm thấy khuyến mãi"]);
        }
    } else {
        http_response_code(500);
        echo json_encode(["success" => false, "message" => "Lỗi khi xóa khuyến mãi: " . $stmt->error]);
    }
    $stmt->close();
}
else{
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Phương thức không được hỗ trợ. Vui lòng dùng DELETE."]);
}
$conn->close();

?>