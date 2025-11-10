<?php

include('../database.php'); // Kết nối CSDL và load .env

$vnp_HashSecret = $_ENV['VNPAY_HASH_SECRET'];
$app_redirect_url = "yourappscheme://payment_result"; // URL chuyển hướng cho ứng dụng di động

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

$order_id_from_vnpay_ref = (int)substr($_GET['vnp_TxnRef'] ?? 0, 0, -10); // Lấy order_id từ vnp_TxnRef
$amount = ($_GET['vnp_Amount'] ?? 0) / 100;
$response_code = $_GET['vnp_ResponseCode'] ?? '99';

if ($secureHash == $vnp_SecureHash) {
    if ($response_code == '00') {
        // THANH TOÁN THÀNH CÔNG

        $conn->begin_transaction();
        try {
            // Cập nhật trạng thái ORDER (BƯỚC 1)
            $sql_update_order = "UPDATE ORDERS SET order_status = 'HoanThanh', order_updated_at = NOW() WHERE order_id = ?";
            $stmt_update_order = $conn->prepare($sql_update_order);
            $stmt_update_order->bind_param("i", $order_id_from_vnpay_ref);
            $stmt_update_order->execute();
            $stmt_update_order->close();
            
            // Lấy table_id
            $sql_table_id = "SELECT table_id FROM ORDERS WHERE order_id = ?";
            $stmt_table = $conn->prepare($sql_table_id);
            $stmt_table->bind_param("i", $order_id_from_vnpay_ref);
            $stmt_table->execute();
            $table_id = $stmt_table->get_result()->fetch_assoc()['table_id'];
            $stmt_table->close();


            // Ghi nhận giao dịch vào bảng PAYMENTS (BƯỚC 2)
            $payment_method = 'ChuyenKhoan';
            $cashier_id = 0; // Giả sử ID 0 là System/Auto
            
            $sql_payment = "INSERT INTO PAYMENTS (order_id, user_id, payment_method, payment_amount_paid) VALUES (?, ?, ?, ?)";
            $stmt_payment = $conn->prepare($sql_payment);
            $stmt_payment->bind_param("iids", $order_id_from_vnpay_ref, $cashier_id, $payment_method, $amount);
            $stmt_payment->execute();
            $stmt_payment->close();

            // Cập nhật trạng thái Bàn về 'Trong' (BƯỚC 3)
            $sql_update_table = "UPDATE TABLES SET status = 'Trong' WHERE table_id = ?";
            $stmt_update_table = $conn->prepare($sql_update_table);
            $stmt_update_table->bind_param("i", $table_id);
            $stmt_update_table->execute();
            $stmt_update_table->close();
            
            $conn->commit();
            
            $redirect_query = http_build_query([
                'payment' => 'success',
                'order_id' => $order_id_from_vnpay_ref
            ]);
            // CHUYỂN HƯỚNG VỀ ỨNG DỤNG DI ĐỘNG/WEB
            header('Location: ' . $app_redirect_url . '?' . $redirect_query);
            exit;

        } catch (Exception $e) {
            $conn->rollback();
            // LỖI XỬ LÝ (VD: CSDL): Vẫn phải thông báo lỗi cho người dùng
            header('Location: ' . $app_redirect_url . '?payment=failed&order_id=' . $order_id_from_vnpay_ref);
            exit;
        }

    } else {
        // THANH TOÁN THẤT BẠI (Mã VNPAY khác 00)
        header('Location: ' . $app_redirect_url . '?payment=failed');
        exit;
    }
} else {
    // LỖI CHỮ KÝ
    header('Location: ' . $app_redirect_url . '?payment=invalid');
    exit;
}
?>