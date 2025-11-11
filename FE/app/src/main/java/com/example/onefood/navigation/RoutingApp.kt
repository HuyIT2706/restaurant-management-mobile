package com.example.onefood.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.onefood.main.auth.ui.LoginScreen
import com.example.onefood.main.home.ui.HomeScreen
import com.example.onefood.main.home.ui.TablesScreen
import com.example.onefood.main.home.ui.OrderScreen
import com.example.onefood.main.home.ui.DetailProductScreen
import com.example.onefood.main.home.ui.UserScreen
import com.example.onefood.main.home.ui.EditUserScreen
import com.example.onefood.main.home.ui.ProductScreen
import com.example.onefood.main.home.ui.AddProductScreen
import com.example.onefood.main.home.ui.EmployeesStaticListScreen
import com.example.onefood.main.home.ui.UpdateProductScreen
import com.example.onefood.main.home.ui.ProductDetailViewScreen
import com.example.onefood.main.home.ui.OrderStaff
import com.example.onefood.main.home.ui.OrderDetailStaff
import com.example.onefood.main.home.ui.PromotionListScreen
import com.example.onefood.main.home.ui.PromotionAddScreen
import com.example.onefood.main.home.ui.PromotionEditScreen
import com.example.onefood.main.home.ui.PromotionDetailScreen
import com.example.onefood.main.home.ui.RevenueListScreen
import com.example.onefood.main.home.ui.RevenueDetailScreen
import com.example.onefood.main.home.ui.CartScreen
import com.example.onefood.main.home.ui.TableOrderDetailScreen
import com.example.onefood.main.home.ui.PaymentScreen


import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun RoutingApp(
	startDestination: String = "login",
	deepLinkPaymentStatus: String? = null,
	deepLinkOrderId: String? = null
) {
	val navController = rememberNavController()
	val context = LocalContext.current

	// Handle deep link from VNPay return
	LaunchedEffect(deepLinkPaymentStatus, deepLinkOrderId) {
		if (!deepLinkPaymentStatus.isNullOrBlank() && !deepLinkOrderId.isNullOrBlank()) {
			when (deepLinkPaymentStatus) {
				"success" -> {
					Toast.makeText(context, "Thanh toán thành công", Toast.LENGTH_SHORT).show()
					val orderDetailRoute = "order_detail_staff_route/$deepLinkOrderId"
					kotlinx.coroutines.delay(500) // Small delay to ensure toast is visible
					
					// Navigate to order detail screen
					// Pop backstack to order detail route (inclusive = true) to remove it if it exists
					// Then navigate again to recreate the screen and trigger data reload
					try {
						// Pop to order detail route with inclusive = true to remove it from backstack
						navController.popBackStack(orderDetailRoute, inclusive = true)
						kotlinx.coroutines.delay(200)
					} catch (e: Exception) {
						// Route doesn't exist in backstack, that's okay
					}
					
					// Now navigate to order detail - this will recreate the screen and trigger LaunchedEffect
					navController.navigate(orderDetailRoute) {
						// Pop up to order list but keep it
						popUpTo("order_list_route") { 
							inclusive = false
							saveState = false
						}
						// Force recreation by not using launchSingleTop
						launchSingleTop = false
						restoreState = false
					}
				}
				"failed" -> {
					Toast.makeText(context, "Thanh toán thất bại", Toast.LENGTH_LONG).show()
					// Stay on payment screen or navigate back to order detail
					val orderDetailRoute = "order_detail_staff_route/$deepLinkOrderId"
					kotlinx.coroutines.delay(300)
					navController.navigate(orderDetailRoute) {
						popUpTo("order_list_route") { inclusive = false }
						launchSingleTop = true
					}
				}
				"invalid" -> {
					Toast.makeText(context, "Xác thực thanh toán không hợp lệ", Toast.LENGTH_LONG).show()
					// Stay on payment screen or navigate back to order detail
					val orderDetailRoute = "order_detail_staff_route/$deepLinkOrderId"
					kotlinx.coroutines.delay(300)
					navController.navigate(orderDetailRoute) {
						popUpTo("order_list_route") { inclusive = false }
						launchSingleTop = true
					}
				}
			}
		}
	}

	NavHost(navController = navController, startDestination = startDestination) {
		composable("login") {
			LoginScreen(navController = navController)
		}

		composable(
			route = "home/{role}",
			arguments = listOf(navArgument("role") { type = NavType.StringType })
		) { backStackEntry ->
			val role = backStackEntry.arguments?.getString("role") ?: ""
			HomeScreen(role = role, navController = navController)
		}

		composable("goi_mon_route") {
			TablesScreen(navController = navController)
		}

		composable(
			route = "order_menu_route/{tableId}",
			arguments = listOf(navArgument("tableId") { type = NavType.IntType })
		) { backStackEntry ->
			val tableId = backStackEntry.arguments?.getInt("tableId") ?: 0
			OrderScreen(tableId = tableId, navController = navController)
		}

		composable("product_route") {
			ProductScreen(navController = navController)
		}

		composable("order_list_route") {
			OrderStaff(navController = navController)
		}

		composable(
			route = "order_detail_staff_route/{orderId}",
			arguments = listOf(navArgument("orderId") { type = NavType.StringType })
		) { backStackEntry ->
			val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
			OrderDetailStaff(navController = navController, orderId = orderId)
		}

		composable(
			route = "payment_route/{orderId}",
			arguments = listOf(navArgument("orderId") { type = NavType.StringType })
		) { backStackEntry ->
			val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
			PaymentScreen(navController = navController, orderId = orderId)
		}

		composable("add_product_route") {
			AddProductScreen(navController = navController)
		}

		composable(
			route = "update_product_route/{productId}/{productName}/{productCategory}/{productPrice}/{productDescription}",
			arguments = listOf(
				navArgument("productId") { type = NavType.IntType },
				navArgument("productName") { type = NavType.StringType },
				navArgument("productCategory") { type = NavType.StringType },
				navArgument("productPrice") { type = NavType.IntType },
				navArgument("productDescription") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val productId = backStackEntry.arguments?.getInt("productId") ?: 0
			val productName = try {
				java.net.URLDecoder.decode(backStackEntry.arguments?.getString("productName") ?: "", "UTF-8")
			} catch (e: Exception) {
				backStackEntry.arguments?.getString("productName") ?: ""
			}
			val productCategory = try {
				java.net.URLDecoder.decode(backStackEntry.arguments?.getString("productCategory") ?: "", "UTF-8")
			} catch (e: Exception) {
				backStackEntry.arguments?.getString("productCategory") ?: ""
			}
			val productPrice = backStackEntry.arguments?.getInt("productPrice") ?: 0
			val productDescription = try {
				val desc = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("productDescription") ?: "", "UTF-8")
				if (desc.isEmpty()) null else desc
			} catch (e: Exception) {
				val desc = backStackEntry.arguments?.getString("productDescription") ?: ""
				if (desc.isEmpty()) null else desc
			}
			UpdateProductScreen(
				navController = navController,
				productId = productId,
				productName = productName,
				productCategory = productCategory,
				productPrice = productPrice,
				productDescription = productDescription
			)
		}

		// Route for product detail from order screen (with quantity, notes, add to cart)
		composable(
			route = "product_detail/{tableId}/{productId}/{productName}/{price}",
			arguments = listOf(
				navArgument("tableId") { type = NavType.IntType },
				navArgument("productId") { type = NavType.IntType },
				navArgument("productName") { type = NavType.StringType },
				navArgument("price") { type = NavType.IntType }
			)
		) { backStackEntry ->
			val tableId = backStackEntry.arguments?.getInt("tableId") ?: 0
			val productId = backStackEntry.arguments?.getInt("productId") ?: 0
			val productName = try {
				java.net.URLDecoder.decode(backStackEntry.arguments?.getString("productName") ?: "", "UTF-8")
			} catch (e: Exception) {
				backStackEntry.arguments?.getString("productName") ?: ""
			}
			val price = backStackEntry.arguments?.getInt("price") ?: 0
			DetailProductScreen(
				navController = navController,
				tableId = tableId,
				productId = productId,
				productName = productName,
				productPrice = price
			)
		}

		// Route for product detail from product management screen (view only)
		composable(
			route = "product_view_detail/{productId}/{productName}/{price}",
			arguments = listOf(
				navArgument("productId") { type = NavType.IntType },
				navArgument("productName") { type = NavType.StringType },
				navArgument("price") { type = NavType.IntType }
			)
		) { backStackEntry ->
			val productId = backStackEntry.arguments?.getInt("productId") ?: 0
			val productName = try {
				java.net.URLDecoder.decode(backStackEntry.arguments?.getString("productName") ?: "", "UTF-8")
			} catch (e: Exception) {
				backStackEntry.arguments?.getString("productName") ?: ""
			}
			val price = backStackEntry.arguments?.getInt("price") ?: 0
			ProductDetailViewScreen(
				navController = navController,
				productId = productId,
				productName = productName,
				productPrice = price
			)
		}

		composable("purchase_history_route") {
			CartScreen(navController = navController)
		}

		// Chi tiết đơn hàng của bàn (từ giỏ hàng)
		composable(
			route = "table_order_detail/{tableId}",
			arguments = listOf(navArgument("tableId") { type = NavType.IntType })
		) { backStackEntry ->
			val tableId = backStackEntry.arguments?.getInt("tableId") ?: 0
			TableOrderDetailScreen(navController = navController, tableId = tableId)
		}

		composable("profile_route") {
			UserScreen(navController = navController)
		}

		composable("edit_user") {
			EditUserScreen(navController = navController)
		}

		// 📊 Thống kê
		composable("statistics_route") {
			RevenueListScreen(navController = navController)
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
			PromotionEditScreen(navController = navController, promotionId = id)
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
		// 👥 Quản lý nhân viên
		composable("user_management_route") {
			EmployeesStaticListScreen(
				onBackPress = {
					navController.popBackStack()
				}
			)
		}
	}
}
