<?php
header('Content-Type: application/json');
include('../database.php');
include('../auth.php');
error_reporting(E_ALL);
ini_set('display_errors', 1);

$user_data = verifyToken();

// CHỈ CÓ NHÂN VIÊN THU NGÂN MỚI CÓ QUYỀN THANH TOÁN
if ($user_data->user_role !== 'ThuNgan' && $user_data->user_role !== 'QuanLy') {
    http_response_code(403);
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền thực hiện chức năng thanh toán.']);
    exit();
}
$cashier_id = $user_data->user_id; // ID của Thu ngân

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    $order_id = filter_var($data['order_id'] ?? null, FILTER_VALIDATE_INT);

    // Giá trị ENUM phải khớp chính xác với CSDL (TienMat)
    $payment_method = 'TienMat';

    if (empty($order_id)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Thiếu ID đơn hàng.']);
        exit();
    }

    // Bắt đầu Transaction
    $conn->begin_transaction();

    try {
        // 1. Lấy thông tin order và table_id (Dùng FOR UPDATE để khóa record)
        $sql_info = "SELECT order_totalamount, table_id, order_status FROM orders WHERE order_id = ? FOR UPDATE";
        $stmt_info = $conn->prepare($sql_info);
        $stmt_info->bind_param("i", $order_id);
        $stmt_info->execute();
        $result_info = $stmt_info->get_result();
        $order_info = $result_info->fetch_assoc();
        $stmt_info->close();

        if (!$order_info) {
            throw new Exception("Không tìm thấy đơn hàng.");
        }
        // Giả sử trạng thái 'HoanThanh' là trạng thái cuối cùng, nên kiểm tra để tránh thanh toán lại
        if ($order_info['order_status'] === 'HoanThanh') {
            throw new Exception("Đơn hàng đã được thanh toán trước đó.");
        }

        $amount_paid = $order_info['order_totalamount'];
        $table_id = $order_info['table_id'];

        $sql_update_order = "UPDATE orders SET order_status = 'HoanThanh', cashier_id = ?, order_updated_at = NOW() WHERE order_id = ?";
        $stmt_update_order = $conn->prepare($sql_update_order);
        
        $stmt_update_order->bind_param("ii", $cashier_id, $order_id); 
        
        $stmt_update_order->execute();
        $stmt_update_order->close();

        // 3. Ghi nhận giao dịch vào bảng PAYMENTS 
        // Cột: order_id, user_id, payment_method, payment_amount_paid
        $sql_payment = "INSERT INTO payments (order_id, user_id, payment_method, payment_amount_paid) VALUES (?, ?, ?, ?)";
        $stmt_payment = $conn->prepare($sql_payment);

        // Kiểu dữ liệu: i (order_id), i (user_id), s (method), d (amount)
        $stmt_payment->bind_param("iisd", $order_id, $cashier_id, $payment_method, $amount_paid);

        $stmt_payment->execute();
        $stmt_payment->close();

        // 4. Cập nhật trạng thái Bàn về 'Trong'
        $sql_update_table = "UPDATE tables SET status = 'Trong' WHERE table_id = ?";
        $stmt_update_table = $conn->prepare($sql_update_table);
        $stmt_update_table->bind_param("i", $table_id);
        $stmt_update_table->execute();
        $stmt_update_table->close();

        // HOÀN TẤT GIAO DỊCH
        $conn->commit();

        http_response_code(200);
        echo json_encode([
            'success' => true,
            'message' => 'Thanh toán tiền mặt thành công. Bàn đã được dọn!',
            'order_id' => $order_id
        ]);
    } catch (Exception $e) {
        // THẤT BẠI: ROLLBACK
        $conn->rollback();
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'message' => 'Lỗi xử lý thanh toán: ' . $e->getMessage()
        ]);
    }
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Phương thức không được hỗ trợ. Vui lòng dùng POST.']);
}

$conn->close();
