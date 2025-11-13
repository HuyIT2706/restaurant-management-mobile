<?php
require_once __DIR__ . '/vendor/autoload.php'; 
use Dotenv\Dotenv;

$dotenv = Dotenv::createImmutable(__DIR__); 
$dotenv->safeLoad();

// 1. LẤY CÁC BIẾN TỪ .env
$servername = $_ENV['DB_HOST'] ?? 'localhost';
$username = $_ENV['DB_USER'] ?? 'pifbzybd_dbappnhahang';
$dbname = $_ENV['DB_NAME'] ?? 'pifbzybd_quanlinhahangmb';
$password = $_ENV['DB_PASS'] ?? 'm4YKkFB8UJ#w6lvB'; 

// 2. THIẾT LẬP KẾT NỐI (Tạo biến $conn)
// Thử kết nối với error reporting tắt để kiểm soát lỗi tốt hơn
mysqli_report(MYSQLI_REPORT_STRICT | MYSQLI_REPORT_ERROR);

try {
    $conn = new mysqli($servername, $username, $password, $dbname);
    
    // 3. THIẾT LẬP CHARSET (Quan trọng cho Tiếng Việt)
    if (!$conn->set_charset("utf8mb4")) {
        error_log("Error setting charset utf8mb4: " . $conn->error);
        // Vẫn tiếp tục nếu không set được charset
    }
    
} catch (mysqli_sql_exception $e) {
    // Log lỗi chi tiết
    error_log("Database connection error: " . $e->getMessage());
    error_log("Host: $servername, User: $username, Database: $dbname");
    
    // Nếu lỗi "Access denied", thử các host khác
    if (strpos($e->getMessage(), 'Access denied') !== false && $servername === 'localhost') {
        // Thử với 127.0.0.1
        try {
            error_log("Trying alternative host: 127.0.0.1");
            $conn = new mysqli('127.0.0.1', $username, $password, $dbname);
            if (!$conn->set_charset("utf8mb4")) {
                error_log("Error setting charset utf8mb4: " . $conn->error);
            }
        } catch (mysqli_sql_exception $e2) {
            // Nếu vẫn lỗi, hiển thị thông báo lỗi rõ ràng
            http_response_code(500);
            die(json_encode([
                'success' => false,
                'message' => 'Database connection failed',
                'error' => 'Cannot connect to database. Please check your database configuration.',
                'details' => 'Check: 1) Database user permissions 2) Database name 3) Password 4) Host (try 127.0.0.1 instead of localhost)'
            ], JSON_UNESCAPED_UNICODE));
        }
    } else {
        // Các lỗi khác
        http_response_code(500);
        die(json_encode([
            'success' => false,
            'message' => 'Database connection failed',
            'error' => $e->getMessage()
        ], JSON_UNESCAPED_UNICODE));
    }
} catch (Exception $e) {
    error_log("Unexpected error: " . $e->getMessage());
    http_response_code(500);
    die(json_encode([
        'success' => false,
        'message' => 'Database connection error',
        'error' => $e->getMessage()
    ], JSON_UNESCAPED_UNICODE));
}

?>

