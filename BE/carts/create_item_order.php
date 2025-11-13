<?php
header('Content-Type: application/json');
include('../database.php'); 
include('../auth.php');      
error_reporting(E_ALL);
ini_set('display_errors', 1);

// BẮT ĐẦU: XÁC MINH TOKEN VÀ PHÂN QUYỀN
$user_data = verifyToken();

if ($user_data->user_role !== 'Order') {
    http_response_code(403); 
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền thêm/sửa món cho đơn hàng.']);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    $order_id = filter_var($data['order_id'] ?? null, FILTER_VALIDATE_INT); 
    $items = $data['items'] ?? []; 
    
    // Kiểm tra dữ liệu bắt buộc
    if (empty($order_id) || empty($items)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Thiếu ID đơn hàng hoặc danh sách món ăn.']);
        exit();
    }
    
    $conn->begin_transaction();
    $success = true;
    
    try {
        
        // =========================================================================
        // BƯỚC 1: XỬ LÝ TỪNG MÓN (Thêm hoặc Cập nhật)
        // =========================================================================
        foreach ($items as $item) {
            $product_id = filter_var($item['product_id'], FILTER_VALIDATE_INT);
            $quantity_added = filter_var($item['quantity'], FILTER_VALIDATE_INT); 
            $price = $item['price']; 
            $notes = $conn->real_escape_string($item['notes'] ?? '');

            if (!$product_id || $quantity_added <= 0 || $price < 0) {
                $success = false;
                throw new Exception("Dữ liệu món ăn không hợp lệ.");
            }
            
            // 1a. KIỂM TRA MÓN ĐÃ TỒN TẠI TRONG ĐƠN HÀNG CHƯA
            $sql_check = "SELECT order_detail_id, order_detail_quantity FROM order_details WHERE order_id = ? AND product_id = ? LIMIT 1";
            $stmt_check = $conn->prepare($sql_check);
            $stmt_check->bind_param("ii", $order_id, $product_id);
            $stmt_check->execute();
            $result_check = $stmt_check->get_result();
            $existing_item = $result_check->fetch_assoc();
            $stmt_check->close();

            if ($existing_item) {
                $new_quantity = $existing_item['order_detail_quantity'] + $quantity_added;
                $sql_update = "UPDATE order_details SET order_detail_quantity = ?, order_detail_notes = ? WHERE order_detail_id = ?";
                $stmt_update = $conn->prepare($sql_update);
                $stmt_update->bind_param("isi", $new_quantity, $notes, $existing_item['order_detail_id']);
                $stmt_update->execute();
                $stmt_update->close();

            } else {
        
                $sql_insert = "INSERT INTO order_details (order_id, product_id, order_detail_quantity, order_detail_price, order_detail_notes) VALUES (?, ?, ?, ?, ?)";
                $stmt_insert = $conn->prepare($sql_insert);
                $stmt_insert->bind_param("iiids", $order_id, $product_id, $quantity_added, $price, $notes);
                $stmt_insert->execute();
                $stmt_insert->close();
            }
        }
        
        // =========================================================================
        // BƯỚC 2: TÍNH VÀ CẬP NHẬT TỔNG TIỀN
        // =========================================================================
        if ($success) {
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
            
            $conn->commit();
            
            http_response_code(200);
            echo json_encode([
                'success' => true, 
                'message' => 'Cập nhật đơn hàng thành công!',
                'order_id' => $order_id,
                'new_total_amount' => $order_total_amount
            ]);
            
        } else {
            throw new Exception("Lỗi xảy ra trong quá trình xử lý món ăn.");
        }
        
    } catch (Exception $e) {
        // THẤT BẠI: ROLLBACK
        $conn->rollback();
        http_response_code(500);
        echo json_encode([
            'success' => false, 
            'message' => 'Lỗi hệ thống khi cập nhật đơn hàng: ' . $e->getMessage()
        ]);
    }
    
} else {
    http_response_code(405); 
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ. Vui lòng dùng POST.']);
}

$conn->close();
?>