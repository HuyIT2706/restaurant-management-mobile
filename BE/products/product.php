<?php
include('../database.php'); 

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *'); 

error_reporting(E_ALL);
ini_set('display_errors', 1);

// check database - charset đã được set trong database.php (utf8mb4)
// Không cần set lại charset, nhưng kiểm tra connection
if (!isset($conn) || $conn->connect_error) {
    http_response_code(500);
    die(json_encode([
        'success' => false,
        'message' => 'Database connection failed',
        'error' => $conn->connect_error ?? 'Connection not established'
    ], JSON_UNESCAPED_UNICODE));
}

$select_cols = "p.product_id, p.product_name, p.product_description, p.price, p.image_url, p.product_active, c.category_name";
$join_clause = "FROM products p JOIN categories c ON p.category_id = c.category_id";
$where_clause = "WHERE p.product_active = TRUE";
$order_clause = "ORDER BY c.category_name, p.product_id ASC";

// Pagination support
$page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
$limit = isset($_GET['limit']) ? max(1, min(100, intval($_GET['limit']))) : 50; // Default 50, max 100
$offset = ($page - 1) * $limit;

// Check id product
if (isset($_GET['id'])) {
    $id = mysqli_real_escape_string($conn, $_GET['id']);
    $sql = "SELECT {$select_cols} {$join_clause} {$where_clause} AND p.product_id = '$id' LIMIT 1";
} else {
    // Get total count for pagination
    $count_sql = "SELECT COUNT(*) as total {$join_clause} {$where_clause}";
    $count_result = mysqli_query($conn, $count_sql);
    $total_count = 0;
    if ($count_result) {
        $count_row = mysqli_fetch_assoc($count_result);
        $total_count = intval($count_row['total']);
    }
    
    $sql = "SELECT {$select_cols} {$join_clause} {$where_clause} {$order_clause} LIMIT $limit OFFSET $offset";
}

$result = mysqli_query($conn, $sql);

if (!$result) {
    die("Lỗi query: " . mysqli_error($conn));
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
    // Trả về danh sách với pagination info
    echo json_encode([
        'data' => $products,
        'pagination' => [
            'page' => $page,
            'limit' => $limit,
            'total' => $total_count,
            'total_pages' => ceil($total_count / $limit)
        ]
    ], JSON_UNESCAPED_UNICODE);
}

$conn->close();
?>