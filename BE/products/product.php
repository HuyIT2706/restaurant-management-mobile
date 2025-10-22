<?php
include('../database.php'); 

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *'); 

error_reporting(E_ALL);
ini_set('display_errors', 1);
try {
    // check database
    if (!mysqli_set_charset($conn, "utf8")) {
        throw new Exception("Lỗi set charset: " . mysqli_error($conn));
    }

    $select_cols = "p.product_id, p.product_name, p.product_description, p.price, p.image_url, p.product_active, c.category_name";
    $join_clause = "FROM PRODUCTS p JOIN CATEGORIES c ON p.category_id = c.category_id";
    $where_clause = "WHERE p.product_active = TRUE";
    $order_clause = "ORDER BY c.category_name, p.product_id ASC";
    // Check id product
    if (isset($_GET['id'])) {
        $id = mysqli_real_escape_string($conn, $_GET['id']);
        $sql = "SELECT {$select_cols} {$join_clause} {$where_clause} AND p.product_id = '$id' LIMIT 1";

    } else {
        $sql = "SELECT {$select_cols} {$join_clause} {$where_clause} {$order_clause}";
    }
    
    $result = mysqli_query($conn, $sql);

    if (!$result) {
        throw new Exception(mysqli_error($conn));
    }

    $products = [];
    while ($row = mysqli_fetch_assoc($result)) {
        $row['price'] = (string)$row['price']; 
        $products[] = $row;
    }

    if (isset($_GET['id'])) {
        // Trả về chi tiết (phần tử đầu tiên hoặc null nếu không tìm thấy)
        echo json_encode($products[0] ?? null, JSON_UNESCAPED_UNICODE);
    } else {
        // Trả về danh sách
        echo json_encode($products, JSON_UNESCAPED_UNICODE);
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage(), 'sql_query' => $sql ?? 'N/A'], JSON_UNESCAPED_UNICODE);
} finally {
    if (isset($conn)) {
        $conn->close();
    }
}
?>