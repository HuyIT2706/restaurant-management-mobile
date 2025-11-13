<?php
header('Content-Type: application/json');
include('../database.php'); 
include('../auth.php'); 
error_reporting(E_ALL);
ini_set('display_errors', 1);

// BẮT ĐẦU: XÁC MINH TOKEN VÀ PHÂN QUYỀN
$user_data = verifyToken();

if ($user_data->user_role !== 'Order' && $user_data->user_role !== 'QuanLy') {
    http_response_code(403); 
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền thực hiện chức năng đặt đơn.']);
    exit();
}
$user_id = $user_data->user_id; 

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    $table_id = filter_var($data['table_id'] ?? null, FILTER_VALIDATE_INT);
    $items = $data['items'] ?? []; 
    
    if (empty($table_id) || empty($items)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Thiếu ID bàn hoặc danh sách món ăn.']);
        exit();
    }
    
    // LOẠI BỎ: BƯỚC 0 (Kiểm tra trạng thái bàn)
    
    // Bắt đầu Transaction
    $conn->begin_transaction();
    $success = true;
    
    try {
        // =========================================================================
        // BƯỚC 1: TẠO BẢN GHI MỚI TRONG ORDERS
        // =========================================================================
        // Bàn phải có trạng thái 'Dang phuc vu' TỪ API CHỌN BÀN TRƯỚC ĐÓ.
        $sql_order = "INSERT INTO orders (table_id, user_id, order_status) VALUES (?, ?, 'TiepNhan')";
        $stmt_order = $conn->prepare($sql_order);
        $stmt_order->bind_param("ii", $table_id, $user_id); 
        $stmt_order->execute();
        $order_id = $conn->insert_id;
        $stmt_order->close();
        
        // =========================================================================
        // BƯỚC 2: CHÈN TẤT CẢ MÓN VÀO ORDER_DETAILS
        // =========================================================================
        $sql_detail = "INSERT INTO order_details (order_id, product_id, order_detail_quantity, order_detail_price, order_detail_notes) VALUES (?, ?, ?, ?, ?)";
        $stmt_detail = $conn->prepare($sql_detail);
        
        foreach ($items as $item) {
            $product_id = filter_var($item['product_id'], FILTER_VALIDATE_INT);
            $quantity = filter_var($item['quantity'], FILTER_VALIDATE_INT);
            $price = $item['price']; 
            $notes = $conn->real_escape_string($item['notes'] ?? '');
            
            if ($product_id && $quantity > 0 && $price >= 0) {
                $stmt_detail->bind_param("iiids", $order_id, $product_id, $quantity, $price, $notes);
                $stmt_detail->execute();
            } else {
                $success = false;
                break; 
            }
        }
        $stmt_detail->close();
        
        if ($success) {
            // =========================================================================
            // BƯỚC 2.5: TÍNH VÀ CẬP NHẬT TỔNG TIỀN
            // =========================================================================
            $sql_total = "SELECT SUM(order_detail_quantity * order_detail_price) AS total FROM order_details WHERE order_id = ?";
            $stmt_total = $conn->prepare($sql_total);
            $stmt_total->bind_param("i", $order_id);
            $stmt_total->execute();
            $result_total = $stmt_total->get_result();
            $total_row = $result_total->fetch_assoc();
            $order_total_amount = $total_row['total'] ?? 0.00;
            $stmt_total->close();

            $sql_update_total = "UPDATE orders SET order_totalamount = ? WHERE order_id = ?";
            $stmt_update_total = $conn->prepare($sql_update_total);
            $stmt_update_total->bind_param("di", $order_total_amount, $order_id);
            $stmt_update_total->execute();
            $stmt_update_total->close();

            // =========================================================================
            // LOẠI BỎ BƯỚC 3 (Cập nhật trạng thái bàn)
            // =========================================================================
            
            // HOÀN TẤT GIAO DỊCH
            $conn->commit();
            
            http_response_code(201); // Created
            echo json_encode([
                'success' => true, 
                'message' => 'Đặt đơn thành công!',
                'order_id' => $order_id,
                'total_amount' => $order_total_amount 
            ]);
            
        } else {
            throw new Exception("Lỗi dữ liệu chi tiết món ăn, hủy giao dịch.");
        }
        
    } catch (Exception $e) {
        // THẤT BẠI: ROLLBACK (Hoàn tác)
        $conn->rollback();
        http_response_code(500);
        echo json_encode([
            'success' => false, 
            'message' => 'Lỗi hệ thống khi đặt đơn: ' . $e->getMessage()
        ]);
    }
    
} else {
    http_response_code(405); 
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ. Vui lòng dùng POST.']);
}

$conn->close();
?>