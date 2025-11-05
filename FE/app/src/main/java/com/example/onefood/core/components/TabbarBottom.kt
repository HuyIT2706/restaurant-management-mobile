package com.example.onefood.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.onefood.R
import com.example.onefood.ui.theme.BoderColor
import com.example.onefood.ui.theme.RedPrimary


@Composable
fun BottomTabBar(navController: NavController, homeRole: String? = null) {
	val backStackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = backStackEntry?.destination?.route ?: navController.currentDestination?.route

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(70.dp)
            .offset(y = (-20).dp)
			.padding(horizontal = 12.dp, vertical = 6.dp)
			.border(width = 1.dp, color = BoderColor, shape = RoundedCornerShape(32.dp))
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.background(color = Color.White, shape = RoundedCornerShape(32.dp)),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceEvenly
		) {
			val homeRoute = homeRole?.let { "home/${it}" } ?: "home/QuanLy"
			val items = listOf(
				Triple(R.drawable.ic_home_hv, "Trang chủ", homeRoute),
				Triple(R.drawable.ic_order, "Gọi món", "goi_mon_route"),
				Triple(R.drawable.ic_product, "Lịch sử mua hàng", "purchase_history_route"),
				Triple(R.drawable.ic_profile, "Tài khoản", "profile_route")
			)

			items.forEach { (iconId, label, route) ->
				val isSelected = when {
					route.startsWith("home") -> currentRoute?.startsWith("home") == true
					else -> currentRoute == route
				}

				if (isSelected) {
					Box(
						modifier = Modifier
							.padding(vertical = 6.dp)
							.background(RedPrimary, shape = RoundedCornerShape(24.dp))
							.clickableWithoutRipple { if (route != currentRoute) navController.navigate(route) }
							.padding(horizontal = 20.dp, vertical = 6.dp),
						contentAlignment = Alignment.Center
					) {
						Row(verticalAlignment = Alignment.CenterVertically) {
							Icon(
								painter = painterResource(id = iconId),
								contentDescription = label,
								tint = Color.White,
								modifier = Modifier.size(32.dp)
							)
							Spacer(modifier = Modifier.width(6.dp))
							Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
						}
					}
				} else {
					IconButton(
						onClick = {
							if (route != currentRoute) {
								navController.navigate(route) {
									launchSingleTop = true
									restoreState = true
								}
							}
						},
						modifier = Modifier.size(56.dp)
					) {
						Icon(
							painter = painterResource(id = iconId),
							contentDescription = label,
							tint = Color(0xFFBDBDBD),
							modifier = Modifier.size(30.dp)
						)
					}
				}
			}
		}
	}
}

private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier =
	this.then(
		Modifier.clickable(
			interactionSource = MutableInteractionSource(),
			indication = null,
			role = Role.Button,
			onClick = onClick
		)
	)

