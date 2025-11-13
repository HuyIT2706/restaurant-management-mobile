<?php
header('Content-Type: application/json');
include('../database.php'); // Kết nối CSDL và load .env
include('../auth.php');      // Hàm xác minh JWT Token
error_reporting(E_ALL);
ini_set('display_errors', 1);

// BẮT ĐẦU: XÁC MINH TOKEN VÀ PHÂN QUYỀN
$user_data = verifyToken();

if ($user_data->user_role !== 'Order') {
    http_response_code(403); // Forbidden
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền xóa món khỏi đơn hàng.']);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    
    // Lấy ID chi tiết đơn hàng từ URL
    $order_detail_id = filter_var($_GET['detail_id'] ?? null, FILTER_VALIDATE_INT);
    
    if (empty($order_detail_id)) {
        http_response_code(400); // Bad Request
        echo json_encode(['success' => false, 'message' => 'Vui lòng cung cấp ID chi tiết đơn hàng (detail_id) hợp lệ.']);
        exit();
    }
    
    $conn->begin_transaction();
    
    try {
        // 1. TÌM order_id LIÊN QUAN VÀ GIÁ TRỊ CŨ (Cần cho việc tính toán lại)
        $sql_find_order = "SELECT order_id FROM order_details WHERE order_detail_id = ?";
        $stmt_find = $conn->prepare($sql_find_order);
        $stmt_find->bind_param("i", $order_detail_id);
        $stmt_find->execute();
        $result_find = $stmt_find->get_result();
        
        if ($result_find->num_rows === 0) {
            $conn->rollback();
            http_response_code(404); // Not Found
            echo json_encode(['success' => false, 'message' => 'Không tìm thấy chi tiết món ăn để xóa.']);
            exit();
        }
        $row_find = $result_find->fetch_assoc();
        $order_id = $row_find['order_id'];
        $stmt_find->close();
        
        // 2. XÓA BẢN GHI KHỎI ORDER_DETAILS
        $sql_delete = "DELETE FROM order_details WHERE order_detail_id = ?";
        $stmt_delete = $conn->prepare($sql_delete);
        $stmt_delete->bind_param("i", $order_detail_id);
        $stmt_delete->execute();
        $stmt_delete->close();
        
        // 3. TÍNH TOÁN LẠI TỔNG TIỀN (order_totalamount)
        $sql_total = "SELECT SUM(order_detail_quantity * order_detail_price) AS total FROM order_details WHERE order_id = ?";
        $stmt_total = $conn->prepare($sql_total);
        $stmt_total->bind_param("i", $order_id);
        $stmt_total->execute();
        $result_total = $stmt_total->get_result();
        $total_row = $result_total->fetch_assoc();
        
        // Nếu không còn món nào, tổng tiền là 0.00
        $order_total_amount = $total_row['total'] ?? 0.00; 
        $stmt_total->close();

        // 4. CẬP NHẬT ORDERS với tổng tiền mới
        $sql_update_total = "UPDATE orders SET order_totalamount = ? WHERE order_id = ?";
        $stmt_update_total = $conn->prepare($sql_update_total);
        $stmt_update_total->bind_param("di", $order_total_amount, $order_id);
        $stmt_update_total->execute();
        $stmt_update_total->close();
        
        // 5. [OPTIONAL] KIỂM TRA ĐƠN HÀNG RỖNG: Nếu tổng tiền = 0 và không còn chi tiết, có thể xóa ORDERS và chuyển bàn về 'Trong'
        if ($order_total_amount == 0) {
             // Logic này có thể phức tạp, tạm thời bỏ qua, chỉ cập nhật total=0
        }
        
        // HOÀN TẤT GIAO DỊCH
        $conn->commit();
        
        http_response_code(200);
        echo json_encode([
            'success' => true, 
            'message' => 'Xóa món khỏi đơn hàng thành công!',
            'order_id' => $order_id,
            'new_total_amount' => $order_total_amount
        ]);
        
    } catch (Exception $e) {
        // THẤT BẠI: ROLLBACK
        $conn->rollback();
        http_response_code(500);
        echo json_encode([
            'success' => false, 
            'message' => 'Lỗi hệ thống khi xóa món ăn: ' . $e->getMessage()
        ]);
    }
    
} else {
    http_response_code(405); 
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ. Vui lòng dùng DELETE.']);
}

$conn->close();
?>