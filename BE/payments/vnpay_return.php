<?php

// Enable error logging
error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('log_errors', 1);

include('../database.php'); // Kết nối CSDL và load .env

$vnp_HashSecret = $_ENV['VNPAY_HASH_SECRET'];
$app_redirect_url = "onefood://payment_result"; // URL chuyển hướng cho ứng dụng di động

// Log all incoming data for debugging
error_log("=== VNPay Return Callback ===");
error_log("GET params: " . print_r($_GET, true));
error_log("REQUEST URI: " . ($_SERVER['REQUEST_URI'] ?? 'N/A'));

// 1. TÁCH DỮ LIỆU VÀ TÍNH TOÁN HASH
$vnp_SecureHash = $_GET['vnp_SecureHash'] ?? '';
$inputData = array();

foreach ($_GET as $key => $value) {
    if (substr($key, 0, 4) == "vnp_") {
        $inputData[$key] = $value;
    }
}

unset($inputData['vnp_SecureHash']);
ksort($inputData);

$hashData = "";
foreach ($inputData as $key => $value) {
    $hashData .= urlencode($key) . "=" . urlencode($value) . '&';
}

$hashData = rtrim($hashData, '&');
$secureHash = hash_hmac('sha512', $hashData, $vnp_HashSecret);

// Extract order_id from vnp_TxnRef
$vnp_TxnRef = $_GET['vnp_TxnRef'] ?? '';
$vnp_OrderInfo = $_GET['vnp_OrderInfo'] ?? '';

error_log("vnp_TxnRef: " . $vnp_TxnRef);
error_log("vnp_OrderInfo: " . $vnp_OrderInfo);

// Try to extract order_id from vnp_OrderInfo first (more reliable)
$order_id_from_vnpay_ref = null;

if (preg_match('/ID:\s*(\d+)/', $vnp_OrderInfo, $matches)) {
    $order_id_from_vnpay_ref = (int)$matches[1];
    error_log("Extracted order_id from vnp_OrderInfo: " . $order_id_from_vnpay_ref);
} else {
    // Fallback: extract from vnp_TxnRef
    if (strlen($vnp_TxnRef) > 10) {
        $order_id_from_vnpay_ref = (int)substr($vnp_TxnRef, 0, -10);
        error_log("Extracted order_id from vnp_TxnRef: " . $order_id_from_vnpay_ref);
    } else {
        error_log("ERROR: Cannot extract order_id from vnp_TxnRef. Length: " . strlen($vnp_TxnRef));
    }
}

$amount = ($_GET['vnp_Amount'] ?? 0) / 100;
$response_code = $_GET['vnp_ResponseCode'] ?? '99';

error_log("Amount: " . $amount);
error_log("Response Code: " . $response_code);
error_log("Secure Hash Match: " . ($secureHash == $vnp_SecureHash ? "YES" : "NO"));
error_log("Calculated Hash: " . $secureHash);
error_log("Received Hash: " . $vnp_SecureHash);

if ($secureHash == $vnp_SecureHash) {
    error_log("Hash verification: SUCCESS");
    
    if ($response_code == '00') {
        error_log("Payment response code: 00 (SUCCESS)");
        
        if (!$order_id_from_vnpay_ref) {
            error_log("ERROR: Cannot extract order_id. Redirecting to app with error.");
            header('Location: ' . $app_redirect_url . '?payment=failed&error=invalid_order_id');
            exit;
        }
        
        // THANH TOÁN THÀNH CÔNG
        error_log("Processing payment for order_id: " . $order_id_from_vnpay_ref);
        
        $conn->begin_transaction();
        try {
            // Kiểm tra order có tồn tại không
            $sql_check_order = "SELECT order_id, table_id, order_status FROM orders WHERE order_id = ?";
            $stmt_check = $conn->prepare($sql_check_order);
            $stmt_check->bind_param("i", $order_id_from_vnpay_ref);
            $stmt_check->execute();
            $order_result = $stmt_check->get_result();
            
            if ($order_result->num_rows == 0) {
                throw new Exception("Order not found: " . $order_id_from_vnpay_ref);
            }
            
            $order_data = $order_result->fetch_assoc();
            $table_id = $order_data['table_id'];
            $current_status = $order_data['order_status'];
            
            error_log("Order found. Current status: " . $current_status . ", Table ID: " . $table_id);
            
            // Kiểm tra nếu đã thanh toán rồi thì không cập nhật nữa
            if ($current_status === 'HoanThanh') {
                error_log("Order already paid. Skipping update.");
                $conn->rollback();
            } else {
                // Cập nhật trạng thái ORDER (BƯỚC 1)
                $sql_update_order = "UPDATE orders SET order_status = 'HoanThanh', order_updated_at = NOW() WHERE order_id = ?";
                $stmt_update_order = $conn->prepare($sql_update_order);
                $stmt_update_order->bind_param("i", $order_id_from_vnpay_ref);
                
                if (!$stmt_update_order->execute()) {
                    throw new Exception("Failed to update order status: " . $stmt_update_order->error);
                }
                
                $affected_rows = $stmt_update_order->affected_rows;
                error_log("Order status updated. Affected rows: " . $affected_rows);
                $stmt_update_order->close();
                
                // Ghi nhận giao dịch vào bảng PAYMENTS (BƯỚC 2)
                // Đảm bảo payment_method chính xác với enum values
                $payment_method = trim('ChuyenKhoan'); // Trim để loại bỏ khoảng trắng
                
                // Validate enum values (TienMat hoặc ChuyenKhoan)
                $allowed_methods = ['TienMat', 'ChuyenKhoan'];
                if (!in_array($payment_method, $allowed_methods)) {
                    error_log("WARNING: Invalid payment_method '$payment_method'. Using 'ChuyenKhoan' as fallback.");
                    $payment_method = 'ChuyenKhoan';
                }
                
                // Log để debug
                error_log("Payment method (before insert): '" . $payment_method . "'");
                error_log("Payment method length: " . strlen($payment_method));
                error_log("Payment method hex: " . bin2hex($payment_method));
                
                $cashier_id = 0; // System/Auto
                
                $sql_payment = "INSERT INTO payments (order_id, user_id, payment_method, payment_amount_paid) VALUES (?, ?, ?, ?)";
                $stmt_payment = $conn->prepare($sql_payment);
                $stmt_payment->bind_param("iids", $order_id_from_vnpay_ref, $cashier_id, $payment_method, $amount);
                
                if (!$stmt_payment->execute()) {
                    $error_msg = "Failed to insert payment record: " . $stmt_payment->error;
                    error_log("ERROR: " . $error_msg);
                    error_log("SQL Error Code: " . $stmt_payment->errno);
                    throw new Exception($error_msg);
                }
                
                error_log("Payment record inserted successfully with payment_method: '" . $payment_method . "'");
                $stmt_payment->close();
                
                // Cập nhật trạng thái Bàn về 'Trong' (BƯỚC 3)
                $sql_update_table = "UPDATE tables SET status = 'Trong' WHERE table_id = ?";
                $stmt_update_table = $conn->prepare($sql_update_table);
                $stmt_update_table->bind_param("i", $table_id);
                
                if (!$stmt_update_table->execute()) {
                    throw new Exception("Failed to update table status: " . $stmt_update_table->error);
                }
                
                error_log("Table status updated to 'Trong'");
                $stmt_update_table->close();
            }
            
            $stmt_check->close();
            $conn->commit();
            error_log("Transaction committed successfully");
            
            $redirect_query = http_build_query([
                'payment' => 'success',
                'order_id' => $order_id_from_vnpay_ref
            ]);
            
            error_log("Redirecting to app: " . $app_redirect_url . '?' . $redirect_query);
            header('Location: ' . $app_redirect_url . '?' . $redirect_query);
            exit;
            
        } catch (Exception $e) {
            $conn->rollback();
            error_log("ERROR in transaction: " . $e->getMessage());
            error_log("Stack trace: " . $e->getTraceAsString());
            
            // LỖI XỬ LÝ (VD: CSDL): Vẫn phải thông báo lỗi cho người dùng
            $redirect_query = http_build_query([
                'payment' => 'failed',
                'order_id' => $order_id_from_vnpay_ref,
                'error' => urlencode($e->getMessage())
            ]);
            header('Location: ' . $app_redirect_url . '?' . $redirect_query);
            exit;
        }
    } else {
        // THANH TOÁN THẤT BẠI (Mã VNPAY khác 00)
        error_log("Payment response code: " . $response_code . " (FAILED)");
        $redirect_query = http_build_query([
            'payment' => 'failed',
            'response_code' => $response_code
        ]);
        if ($order_id_from_vnpay_ref) {
            $redirect_query .= '&order_id=' . $order_id_from_vnpay_ref;
        }
        header('Location: ' . $app_redirect_url . '?' . $redirect_query);
        exit;
    }
} else {
    // LỖI CHỮ KÝ
    error_log("ERROR: Hash verification FAILED");
    error_log("Expected: " . $secureHash);
    error_log("Received: " . $vnp_SecureHash);
    
    $redirect_query = http_build_query([
        'payment' => 'invalid',
        'error' => 'hash_mismatch'
    ]);
    if ($order_id_from_vnpay_ref) {
        $redirect_query .= '&order_id=' . $order_id_from_vnpay_ref;
    }
    header('Location: ' . $app_redirect_url . '?' . $redirect_query);
    exit;
}

?>