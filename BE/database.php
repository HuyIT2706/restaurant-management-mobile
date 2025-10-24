<?php
require_once __DIR__ . '/vendor/autoload.php'; 
use Dotenv\Dotenv;

$dotenv = Dotenv::createImmutable(__DIR__); 
$dotenv->safeLoad();

// 1. LẤY CÁC BIẾN TỪ .env
$servername = $_ENV['DB_HOST'] ?? '127.0.0.1:3307';  // gộp port trực tiếp
$username = $_ENV['DB_USER'] ?? 'root';
$password = $_ENV['DB_PASS'] ?? 'bangbang';
$dbname = $_ENV['DB_NAME'] ?? 'quanlinhahangmb';

// 2. THIẾT LẬP KẾT NỐI (Tạo biến $conn)
$conn = new mysqli($servername, $username, $password, $dbname);


// 3. KIỂM TRA KẾT NỐI
if ($conn->connect_error) {
    // Thông báo lỗi rõ ràng và dừng chương trình nếu kết nối thất bại
    die("Connection failed: " . $conn->connect_error);
}

// 4. THIẾT LẬP CHARSET (Quan trọng cho Tiếng Việt)
if (!$conn->set_charset("utf8")) {
    die("Error loading character set utf8: " . $conn->error);
}


?>