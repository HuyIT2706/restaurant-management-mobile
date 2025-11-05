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

/**
 * Centralized routing for the app. Use this composable from the Activity's setContent.
 */
@Composable
fun RoutingApp(startDestination: String = "login") {
	val navController = rememberNavController()

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

		// Gọi món (chọn bàn)
		composable("goi_mon_route") {
			TablesScreen(navController = navController)
		}


	}
}

