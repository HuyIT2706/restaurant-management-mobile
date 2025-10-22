CREATE DATABASE  IF NOT EXISTS `quanlinhahangmb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `quanlinhahangmb`;
-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: quanlinhahangmb
-- ------------------------------------------------------
-- Server version	8.4.6

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `category_name` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (4,'Hải sản'),(1,'Lẩu'),(3,'Món chay'),(6,'Món nướng'),(7,'Nước uống'),(5,'Rau'),(2,'Súp');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_details`
--

DROP TABLE IF EXISTS `order_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_details` (
  `order_detail_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL,
  `product_id` int DEFAULT NULL,
  `order_detail_quantity` int NOT NULL,
  `order_detail_price` decimal(10,2) NOT NULL,
  `order_detail_notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`order_detail_id`),
  KEY `order_id` (`order_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `order_details_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE,
  CONSTRAINT `order_details_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_details`
--

LOCK TABLES `order_details` WRITE;
/*!40000 ALTER TABLE `order_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_promotions`
--

DROP TABLE IF EXISTS `order_promotions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_promotions` (
  `order_promo_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL,
  `promo_id` int DEFAULT NULL,
  `order_promo_amount` decimal(10,2) NOT NULL,
  PRIMARY KEY (`order_promo_id`),
  UNIQUE KEY `unique_order_promo` (`order_id`,`promo_id`),
  KEY `promo_id` (`promo_id`),
  CONSTRAINT `order_promotions_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `order_promotions_ibfk_2` FOREIGN KEY (`promo_id`) REFERENCES `promotions` (`promo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_promotions`
--

LOCK TABLES `order_promotions` WRITE;
/*!40000 ALTER TABLE `order_promotions` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_promotions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `table_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `order_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `order_status` enum('TiepNhan','DaCheBien','HoanThanh','Huy') NOT NULL DEFAULT 'TiepNhan',
  `order_totalamount` decimal(10,2) DEFAULT '0.00',
  `order_updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  KEY `table_id` (`table_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`table_id`) REFERENCES `tables` (`table_id`),
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `payment_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `payment_method` enum('TienMat','ChuyenKhoan') NOT NULL,
  `payment_amount_paid` decimal(10,2) NOT NULL,
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `order_id` (`order_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `payments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` int NOT NULL AUTO_INCREMENT,
  `category_id` int DEFAULT NULL,
  `product_name` varchar(200) NOT NULL,
  `product_description` text,
  `price` decimal(10,2) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `product_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`product_id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,4,'Cá hồi xông khói ','Cá hồi ngon lắm.',50000.00,'http://localhost/BeMobile/BE/assets/products/trasua-matcha.jpg',1),(2,4,'Tôm sú luộc rần thịt','Tôm sú luộc chín tới, thịt chắc và ngọt, giữ nguyên hương vị biển.',180000.00,'http://localhost/BeMobile/BE/assets/products/haisan-tom_su_luot_ran_thit.png',1),(3,4,'Tôm sú rang muối','Tôm sú rang muối thơm phức, giòn rụm với lớp muối thấm đều.',190000.00,'http://localhost/BeMobile/BE/assets/products/haisan-tom-su-rang-muoi.jpeg',1),(4,4,'Tôm sú rang bơ tỏi','Tôm sú rang bơ tỏi thơm lừng, béo ngậy và đậm đà hương vị.',200000.00,'http://localhost/BeMobile/BE/assets/products/haisan-tom-su-rang-bo-toi.jpeg',1),(5,4,'Cua luộc','Cua luộc tươi ngon, thịt chắc, ăn kèm nước chấm chua cay đậm đà.',220000.00,'http://localhost/BeMobile/BE/assets/products/haisan-cua-luoc.jpg',1),(6,4,'Hàu tươi sống','Hàu tươi sống được trình bày đẹp mắt, kèm nước mắm chua cay đặc trưng.',150000.00,'http://localhost/BeMobile/BE/assets/products/haisan-hau.jpg',1),(7,4,'Tôm chiên giòn','Tôm chiên giòn vàng ruộm, thơm ngon, ăn kèm sốt mayonnaise đặc biệt.',170000.00,'http://localhost/BeMobile/BE/assets/products/haisan-tom-chien-gion.jpg',1),(8,1,'Lẩu 4 ngăn','Nồi lẩu với 4 ngăn chứa các loại nước dùng khác nhau, cho phép thưởng thức đa dạng vị cay, thanh, và ngọt trong cùng một bữa ăn.',430000.00,'http://localhost/BeMobile/BE/assets/products/lau-4-ngan.jpg',1),(9,1,'Lẩu 3 ngăn','Lẩu 3 ngăn phong cách Trung Hoa, với nước dùng cay, ngọt và thanh, kèm đa dạng nguyên liệu tươi ngon như thịt bò, đậu hũ và rau củ.',400000.00,'http://localhost/BeMobile/BE/assets/products/lau-3-ngan.jpg',1),(10,1,'Lẩu thập cẩm','Nồi lẩu thập cẩm nhiều loại nguyên liệu như hải sản, rau củ, nấm và thịt, nước dùng đậm đà thích hợp cho bữa ăn gia đình.',320000.00,'http://localhost/BeMobile/BE/assets/products/lau-thap-cam.jpg',1),(11,1,'Lẩu bao tử bò','Lẩu đặc trưng với bao tử bò cùng nước dùng cay nồng, dùng kèm nhiều loại rau và gia vị phong phú.',380000.00,'http://localhost/BeMobile/BE/assets/products/lau-bao-tu-bo.jpg',1),(12,1,'Lẩu bao ngư','Lẩu hải sản cao cấp với bao ngư, mực, tôm và các loại nấm, nước dùng thơm ngon đậm đà.',420000.00,'http://localhost/BeMobile/BE/assets/products/lau-bao-ngu.jpg',1),(13,1,'Lẩu bò','Lẩu truyền thống với nước dùng đậm đà và thịt bò thái lát mỏng, kèm rau tươi và đồ nhúng phong phú.',300000.00,'http://localhost/BeMobile/BE/assets/products/lau-bo.jpg',1),(14,1,'Lẩu cá tầm','Lẩu cá tầm thanh ngọt, kết hợp với rau củ tươi và gia vị đặc trưng, tạo nên hương vị độc đáo.',390000.00,'http://localhost/BeMobile/BE/assets/products/lau-ca-tam.jpg',1),(15,1,'Lẩu Thái Tom Yum','Lẩu Thái chua cay Tom Yum với hương thơm đặc trưng của sả, lá chanh và ớt, vị đậm đà kích thích vị giác.',350000.00,'http://localhost/BeMobile/BE/assets/products/lau-thai-tomyum.jpeg',1),(16,1,'Lẩu Thái nấm chay','Lẩu Thái chay thanh nhẹ với các loại nấm tươi ngon và nước dùng chua cay đặc trưng.',320000.00,'http://localhost/BeMobile/BE/assets/products/lau-thai-nam-chay.png',1),(17,3,'Đậu hũ xào nấm','Đậu hũ chiên giòn xào cùng nấm tươi và rau củ, thơm ngon, đậm đà hương vị chay.',70000.00,'http://localhost/BeMobile/BE/assets/products/chay-dau-hu-xao-nam.png',1),(18,3,'Chả cuốn nấm','Cuốn chay với bánh tráng mềm, nhân nấm và rau tươi, ăn kèm nước chấm chua ngọt đặc trưng.',65000.00,'http://localhost/BeMobile/BE/assets/products/chay-cuon-nam.png',1),(19,3,'Chả chiên quế chay','Chả chay giòn bên ngoài, mềm dai bên trong, gia vị quế thơm nhẹ, phục vụ kèm nước mắm chay.',60000.00,'http://localhost/BeMobile/BE/assets/products/chay-cha-que.png',1),(20,3,'Bún trộn chay','Bún trộn thanh đạm với rau củ, đậu hũ chiên, nấm và nước chấm đặc biệt.',70000.00,'http://localhost/BeMobile/BE/assets/products/chay-bun-tron.png',1),(21,3,'Bún riêu chay','Món bún riêu chay với nước dùng chua nhẹ, đậu hũ, cà chua, và rau thơm tươi.',75000.00,'http://localhost/BeMobile/BE/assets/products/chay-bun-rieu.png',1),(22,3,'Bột chiên chay','Bột chiên giòn, ăn kèm trứng, rau thơm và nước sốt cay đặc trưng.',65000.00,'http://localhost/BeMobile/BE/assets/products/chay-bot-chien.jpg',1),(23,6,'Heo nướng giòn da','Thịt heo nướng giòn da, mềm mọng, thơm lừng với gia vị đậm đà.',150000.00,'http://localhost/BeMobile/BE/assets/products/nuong-heo-gion-bi.jpg',1),(24,6,'Gà nướng thảo mộc','Gà nướng ướp thảo mộc thơm phức, da vàng giòn, thịt mềm ngon.',140000.00,'http://localhost/BeMobile/BE/assets/products/nuong-ga.png',1),(25,6,'Thịt cừu nướng','Thịt cừu tươi nướng vừa chín tới, giữ nguyên vị ngọt tự nhiên và hương thơm đặc trưng.',180000.00,'http://localhost/BeMobile/BE/assets/products/nuong-thit-cuu.jpg',1),(26,6,'Sườn heo nướng mật ong','Sườn heo nướng mềm, thấm vị mật ong ngọt dịu, kết hợp nước sốt đặc biệt.',160000.00,'http://localhost/BeMobile/BE/assets/products/nuong-suon-heo.jpg',1),(27,6,'Xúc xích nướng','Xúc xích nướng vàng ươm, thơm ngon, ăn kèm bánh mì hoặc rau sống.',90000.00,'http://localhost/BeMobile/BE/assets/products/nuong-xuc-xich.jpg',1),(28,6,'Thịt nướng rau củ','Thịt nướng kết hợp cùng rau củ tươi ngon, thơm lừng và đầy màu sắc.',140000.00,'http://localhost/BeMobile/BE/assets/products/nuong-thit-rau-cu.jpg',1),(29,6,'Sườn cừu nướng BBQ','Sườn cừu nướng BBQ cay nhẹ, thấm đẫm gia vị, mềm và đậm đà hương vị.',190000.00,'http://localhost/BeMobile/BE/assets/products/nuong-suon-cuu.jpg',1),(30,7,'Pepsi lon','Nước ngọt Pepsi lon lạnh sảng khoái, giải khát nhanh chóng.',20000.00,'http://localhost/BeMobile/BE/assets/products/nuocuong-pepsi.jpg',1),(31,7,'Trà đào chanh sả','Trà đào thanh mát kết hợp chanh và sả thơm dịu, giải nhiệt ngày hè.',35000.00,'http://localhost/BeMobile/BE/assets/products/nuocuong-tra-dao-chanh-sa.jpg',1),(32,7,'Soda chanh','Soda chanh sủi tăm thơm mát, tươi ngon, kích thích vị giác.',30000.00,'http://localhost/BeMobile/BE/assets/products/nuonguong-soda-chanh.jpg',1),(33,7,'Trà chanh','Trà chanh truyền thống đậm đà, thơm nồng hương chanh tươi.',25000.00,'http://localhost/BeMobile/BE/assets/products/nuonguong-tra-chanh.jpg',1),(34,7,'Coca Cola lon','Nước ngọt Coca Cola lon đặc trưng, vị ngọt đậm đà, kích thích vị giác.',20000.00,'http://localhost/BeMobile/BE/assets/products/nuocuong-coca.jpg',1),(35,5,'Salad rau củ quả','Salad tươi mát với các loại rau xanh và cà chua bi, hòa quyện cùng nước sốt đặc biệt.',70000.00,'http://localhost/BeMobile/BE/assets/products/rau-salad-ca-chua.jpg',1),(36,5,'Salad xà lách','Xà lách tươi giòn ăn kèm nước sốt kem thanh mát, thích hợp cho bữa ăn nhẹ và dinh dưỡng.',65000.00,'http://localhost/BeMobile/BE/assets/products/rau-salad-xa-lach.jpg',1),(37,5,'Salad cà chua','Cà chua đỏ mọng kết hợp với rau thơm, tạo nên món salad thanh đạm và dễ ăn.',60000.00,'http://localhost/BeMobile/BE/assets/products/rau-salad-cachua.jpg',1),(38,5,'Salad tôm hùm','Salad tươi ngon với tôm hùm kết hợp rau xanh, tạo hương vị đậm đà, sang trọng.',120000.00,'http://localhost/BeMobile/BE/assets/products/rau-salad-tomhum.jpg',1),(39,5,'Rau su hào sợi','Su hào bào sợi tươi ngon, trộn cùng nước sốt chua cay đặc trưng, món ăn thanh nhẹ, dễ tiêu.',70000.00,'http://localhost/BeMobile/BE/assets/products/rau-xu-hao-soi.jpg',1),(40,2,'Súp bí ngô','Súp bí ngô mịn màng, thơm ngọt, trang trí dầu giấm balsamic và rau mùi tươi.',80000.00,'http://localhost/BeMobile/BE/assets/products/sup-bi-ngo.jpg',1),(41,2,'Súp bào ngư hải sâm','Súp hải sản thượng hạng với bào ngư, hải sâm và tôm tươi, nước dùng đậm đà.',150000.00,'http://localhost/BeMobile/BE/assets/products/sup-bao-ngu-hai-sam.jpeg',1),(42,2,'Súp hải sản','Súp hải sản đa dạng với tôm, mực, nấm kim châm và rau củ tươi ngon.',90000.00,'http://localhost/BeMobile/BE/assets/products/sup-hai-san.jpg',1),(43,2,'Súp su hào','Súp su hào dịu nhẹ, kết hợp cùng thịt mềm và cà rốt, mang đến hương vị thanh thanh, bổ dưỡng và dễ ăn.',80000.00,'http://localhost/BeMobile/BE/assets/products/sup-su-hao.jpg',1),(44,2,'Súp củ cải','Súp củ cải trắng béo ngậy, điểm xuyến hạt hạch và dầu ô liu thơm lừng.',85000.00,'http://localhost/BeMobile/BE/assets/products/sup-cu-cai.jpg',1);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promotions`
--

DROP TABLE IF EXISTS `promotions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotions` (
  `promo_id` int NOT NULL AUTO_INCREMENT,
  `promo_code` varchar(50) DEFAULT NULL,
  `promo_type` enum('PhanTram','SoTien') NOT NULL,
  `promo_value` decimal(10,2) NOT NULL,
  `promo_quantity` int DEFAULT NULL,
  `promo_desc` varchar(200) DEFAULT NULL,
  `promo_min_order_amount` decimal(10,2) DEFAULT '0.00',
  `promo_start_date` datetime NOT NULL,
  `promo_end_date` datetime NOT NULL,
  `promo_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`promo_id`),
  UNIQUE KEY `promo_code` (`promo_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotions`
--

LOCK TABLES `promotions` WRITE;
/*!40000 ALTER TABLE `promotions` DISABLE KEYS */;
/*!40000 ALTER TABLE `promotions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tables`
--

DROP TABLE IF EXISTS `tables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tables` (
  `table_id` int NOT NULL AUTO_INCREMENT,
  `table_name` varchar(20) NOT NULL,
  `status` enum('Trong','Dang phuc vu') NOT NULL DEFAULT 'Trong',
  PRIMARY KEY (`table_id`),
  UNIQUE KEY `table_name` (`table_name`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tables`
--

LOCK TABLES `tables` WRITE;
/*!40000 ALTER TABLE `tables` DISABLE KEYS */;
INSERT INTO `tables` VALUES (1,'Bàn 1','Trong'),(2,'Bàn 2','Trong'),(3,'Bàn 3','Trong'),(4,'Bàn 4','Trong'),(5,'Bàn 5','Trong'),(6,'Bàn 6','Trong'),(7,'Bàn 7','Trong'),(8,'Bàn 8','Trong'),(9,'Bàn 9','Trong'),(10,'Bàn 10','Trong'),(11,'Bàn 11','Trong'),(12,'Bàn 12','Trong'),(13,'Bàn 13','Trong'),(14,'Bàn 14','Trong'),(15,'Bàn 15','Dang phuc vu');
/*!40000 ALTER TABLE `tables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `user_phone` varchar(15) NOT NULL,
  `user_password` varchar(255) NOT NULL,
  `user_firstname` varchar(50) NOT NULL,
  `user_lastname` varchar(50) NOT NULL,
  `user_gender` varchar(15) DEFAULT NULL,
  `user_wage` decimal(10,2) NOT NULL,
  `user_image` varchar(255) DEFAULT NULL,
  `user_role` enum('QuanLy','ThuNgan','Order') NOT NULL,
  `user_status` tinyint(1) DEFAULT '1',
  `user_created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `user_updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_phone` (`user_phone`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'0868698389','$2y$10$s6zXO.feZHwhhJoeNGfExujE9GDAxWnimzMt.aQk7aWxkTYKTTrR.','Bùi','Văn Huy','Nam',5000000.00,NULL,'QuanLy',1,'2025-10-19 23:35:41','2025-10-19 23:36:46'),(2,'0363971603','$2y$10$rKkOhRwCu0jY2XLd3rVNt.M7txw8RvViM8Q0DvlRn3TQECjjyKreC','Lê','Thị Tuyết Băng','Nữ',5000000.00,NULL,'Order',1,'2025-10-22 10:08:55','2025-10-22 10:12:13'),(4,'0845511205','$2y$10$tzUlUiC3cqByzjg0fdXSVOj1FrFNkr3tZDroF7dAEQLTm1roVhpQK','Kiều','Trần Thu Uyên','Nữ',5000000.00,NULL,'ThuNgan',1,'2025-10-22 10:11:35','2025-10-22 10:12:38');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-22 10:20:42
