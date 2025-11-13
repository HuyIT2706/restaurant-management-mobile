<?php
header('Content-Type: application/json');
include('../database.php'); 
include('../auth.php'); 

$user_data = verifyToken(); 
if ($user_data->user_role !== 'Order' && $user_data->user_role !== 'ThuNgan' && $user_data->user_role !== 'QuanLy') {
    http_response_code(403); 
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền xem chi tiết đơn hàng.']);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Lấy order_id từ query string (ví dụ: ?order_id=123)
    $order_id = filter_var($_GET['order_id'] ?? null, FILTER_VALIDATE_INT);

    if (empty($order_id)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Thiếu ID đơn hàng.']);
        exit();
    }
    
    try {
        // 1. Truy vấn thông tin cơ bản của đơn hàng
        $sql_order = "
            SELECT 
                o.order_id, o.order_date, o.order_status, o.order_totalamount, o.order_updated_at,
                t.table_name, t.table_id,
                CONCAT(u_order.user_firstname, ' ', u_order.user_lastname) AS order_user_name,
                CONCAT(u_cash.user_firstname, ' ', u_cash.user_lastname) AS cash_user_name
                
            FROM orders o
            JOIN tables t ON o.table_id = t.table_id
            JOIN users u_order ON o.user_id = u_order.user_id
            -- Thêm cột cashier_id vào bảng orders nếu chưa có, để join với Thu ngân
            LEFT JOIN users u_cash ON o.cashier_id = u_cash.user_id 
            WHERE o.order_id = ?
        ";
        $stmt_order = $conn->prepare($sql_order);
        $stmt_order->bind_param("i", $order_id);
        $stmt_order->execute();
        $result_order = $stmt_order->get_result();
        $order_info = $result_order->fetch_assoc();
        $stmt_order->close();

        if (!$order_info) {
            http_response_code(404);
            echo json_encode(['success' => false, 'message' => 'Không tìm thấy đơn hàng.']);
            exit();
        }

        // 2. Truy vấn chi tiết món ăn
        $sql_details = "
            SELECT 
                od.order_detail_quantity, od.order_detail_price, od.order_detail_notes, 
                p.product_name, p.image_url
            FROM order_details od
            JOIN products p ON od.product_id = p.product_id
            WHERE od.order_id = ?
        ";
        $stmt_details = $conn->prepare($sql_details);
        $stmt_details->bind_param("i", $order_id);
        $stmt_details->execute();
        $result_details = $stmt_details->get_result();
        $order_items = $result_details->fetch_all(MYSQLI_ASSOC);
        $stmt_details->close();
        
        // 3. Phản hồi cho Frontend
        $response = array_merge($order_info, ['items' => $order_items]);

        http_response_code(200);
        echo json_encode([
            'success' => true, 
            'data' => $response
        ]);

    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode([
            'success' => false, 
            'message' => 'Lỗi hệ thống: ' . $e->getMessage()
        ]);
    }
    
} else {
    http_response_code(405); 
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ. Vui lòng dùng GET.']);
}

$conn->close();
?>