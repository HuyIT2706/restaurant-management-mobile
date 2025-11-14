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
    $promo_code = $data['promo_code'] ?? null;
    $promo_id = filter_var($data['promo_id'] ?? null, FILTER_VALIDATE_INT);

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

        $order_totalamount = floatval($order_info['order_totalamount']);
        $table_id = $order_info['table_id'];
        
        // 2. Xử lý promotion nếu có
        $discount_amount = 0;
        $final_amount = $order_totalamount;
        $promotion_id = null;
        
        if (!empty($promo_code) || !empty($promo_id)) {
            // Validate promotion
            if (!empty($promo_id)) {
                // Sử dụng promo_id nếu có
                $sql_promo = "SELECT 
                                promo_id, 
                                promo_type, 
                                promo_value,
                                promo_quantity,
                                (SELECT COUNT(*) FROM order_promotions WHERE promo_id = promotions.promo_id) AS used_count
                            FROM promotions
                            WHERE promo_id = ? AND promo_active = 1";
                $stmt_promo = $conn->prepare($sql_promo);
                $stmt_promo->bind_param("i", $promo_id);
            } else if (!empty($promo_code)) {
                // Hoặc sử dụng promo_code
                $sql_promo = "SELECT 
                                promo_id, 
                                promo_type, 
                                promo_value,
                                promo_quantity,
                                (SELECT COUNT(*) FROM order_promotions WHERE promo_id = promotions.promo_id) AS used_count
                            FROM promotions
                            WHERE promo_code = ? AND promo_active = 1";
                $stmt_promo = $conn->prepare($sql_promo);
                $stmt_promo->bind_param("s", $promo_code);
            }
            
            $stmt_promo->execute();
            $result_promo = $stmt_promo->get_result();
            
            if ($result_promo && $result_promo->num_rows > 0) {
                $promotion = $result_promo->fetch_assoc();
                $promotion_id = intval($promotion['promo_id']);
                $promo_type = $promotion['promo_type'];
                $promo_value = floatval($promotion['promo_value']);
                
                // Kiểm tra số lượng còn lại
                $used_count = intval($promotion['used_count']);
                $promo_quantity = intval($promotion['promo_quantity']);
                if ($promo_quantity > 0 && $used_count >= $promo_quantity) {
                    throw new Exception("Mã khuyến mãi đã hết số lượng sử dụng!");
                }
                
                // Tính toán discount
                if ($promo_type === 'PhanTram') {
                    // Giảm theo phần trăm
                    $discount_amount = ($order_totalamount * $promo_value) / 100;
                } else if ($promo_type === 'SoTien') {
                    // Giảm theo số tiền cố định
                    $discount_amount = $promo_value;
                    // Đảm bảo không giảm quá số tiền đơn hàng
                    if ($discount_amount > $order_totalamount) {
                        $discount_amount = $order_totalamount;
                    }
                }
                
                $final_amount = $order_totalamount - $discount_amount;
                if ($final_amount < 0) {
                    $final_amount = 0;
                }
            }
            $stmt_promo->close();
        }
        
        $amount_paid = $final_amount;

        // 3. Cập nhật order_totalamount với số tiền sau discount và trạng thái
        $sql_update_order = "UPDATE orders SET order_status = 'HoanThanh', cashier_id = ?, order_totalamount = ?, order_updated_at = NOW() WHERE order_id = ?";
        $stmt_update_order = $conn->prepare($sql_update_order);
        
        $stmt_update_order->bind_param("idi", $cashier_id, $amount_paid, $order_id); 
        
        $stmt_update_order->execute();
        $stmt_update_order->close();

        // 4. Ghi nhận promotion vào order_promotions nếu có
        if ($promotion_id != null && $discount_amount > 0) {
            // Kiểm tra xem đã có promotion nào cho order này chưa (tránh duplicate)
            $sql_check_promo = "SELECT order_promo_id FROM order_promotions WHERE order_id = ? AND promo_id = ?";
            $stmt_check_promo = $conn->prepare($sql_check_promo);
            $stmt_check_promo->bind_param("ii", $order_id, $promotion_id);
            $stmt_check_promo->execute();
            $result_check_promo = $stmt_check_promo->get_result();
            
            if ($result_check_promo->num_rows == 0) {
                // Chưa có, insert mới
                $sql_order_promo = "INSERT INTO order_promotions (order_id, promo_id, order_promo_amount) VALUES (?, ?, ?)";
                $stmt_order_promo = $conn->prepare($sql_order_promo);
                $stmt_order_promo->bind_param("iid", $order_id, $promotion_id, $discount_amount);
                $stmt_order_promo->execute();
                $stmt_order_promo->close();
            } else {
                // Đã có, update amount
                $sql_update_promo = "UPDATE order_promotions SET order_promo_amount = ? WHERE order_id = ? AND promo_id = ?";
                $stmt_update_promo = $conn->prepare($sql_update_promo);
                $stmt_update_promo->bind_param("dii", $discount_amount, $order_id, $promotion_id);
                $stmt_update_promo->execute();
                $stmt_update_promo->close();
            }
            $stmt_check_promo->close();
        }

        // 5. Ghi nhận giao dịch vào bảng PAYMENTS 
        // Cột: order_id, user_id, payment_method, payment_amount_paid
        $sql_payment = "INSERT INTO payments (order_id, user_id, payment_method, payment_amount_paid) VALUES (?, ?, ?, ?)";
        $stmt_payment = $conn->prepare($sql_payment);

        // Kiểu dữ liệu: i (order_id), i (user_id), s (method), d (amount)
        $stmt_payment->bind_param("iisd", $order_id, $cashier_id, $payment_method, $amount_paid);

        $stmt_payment->execute();
        $stmt_payment->close();

        // 6. Cập nhật trạng thái Bàn về 'Trong'
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
