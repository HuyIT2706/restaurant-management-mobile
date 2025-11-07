package com.example.onefood.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.onefood.main.auth.ui.LoginScreen
import com.example.onefood.main.home.ui.HomeScreen
import com.example.onefood.main.home.ui.TablesScreen
import com.example.onefood.main.revenue.ui.RevenueListScreen
import com.example.onefood.main.revenue.ui.RevenueDetailScreen

// 🎁 Import đầy đủ khuyến mãi
import com.example.onefood.main.promotion.ui.PromotionListScreen
import com.example.onefood.main.promotion.ui.PromotionAddScreen
import com.example.onefood.main.promotion.ui.PromotionEditScreen
import com.example.onefood.main.promotion.ui.PromotionDetailScreen

/**
 * Centralized routing for the app.
 */
@Composable
fun RoutingApp(startDestination: String = "login") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        // 🔐 Đăng nhập
        composable("login") {
            LoginScreen(navController = navController)
        }

        // 🏠 Trang chủ
        composable(
            route = "home/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            HomeScreen(role = role, navController = navController)
        }

        // 🍽 Gọi món (chọn bàn)
        composable("goi_mon_route") {
            TablesScreen(navController = navController)
        }

        // 🛍 Sản phẩm
        composable("product_route") {
            PlaceholderScreen("Màn hình Sản phẩm")
        }

        // 📦 Đơn hàng
        composable("order_list_route") {
            PlaceholderScreen("Màn hình Đơn hàng")
        }

        // 👥 Nhân viên
        composable("user_management_route") {
            PlaceholderScreen("Màn hình Quản lý nhân viên")
        }

        // 📊 Thống kê
        composable("statistics_route") {
            PlaceholderScreen("Màn hình Thống kê")
        }

        // 🎁 Danh sách khuyến mãi
        composable("promotion_management_route") {
            PromotionListScreen(navController = navController)
        }

        // ➕ Thêm khuyến mãi
        composable("promotion_add") {
            PromotionAddScreen(navController = navController)
        }

        // ✏️ Cập nhật khuyến mãi
        composable(
            route = "promotion_edit/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            PromotionEditScreen(navController = navController, id = id)
        }

        // 🔍 Chi tiết khuyến mãi
        composable(
            route = "promotion_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            PromotionDetailScreen(navController = navController, id = id)
        }

        // 💰 Danh sách doanh thu
        composable("revenue_list_route") {
            RevenueListScreen(navController = navController)
        }

        // 📋 Chi tiết doanh thu
        composable(
            route = "revenue_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            RevenueDetailScreen(id = id, navController = navController)
        }
    }
}

/**
 * 🧩 Placeholder cho các màn hình chưa hoàn thiện
 */
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}
