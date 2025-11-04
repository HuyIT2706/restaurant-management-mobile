package com.example.onefood.main.home.ui

import android.hardware.camera2.params.BlackLevelPattern
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.onefood.R
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import com.example.onefood.ui.theme.BeigeLight
import com.example.onefood.core.components.BottomTabBar

data class HomeFeature(
    val title: String,
    val iconId: Int,
    val backgroundColor: Color,
    val route: String,
    val requiredRole: String? = null
)

@Composable
fun HomeScreen(navController: NavController, role: String = "QuanLy") {

    val features = listOf(
        HomeFeature("Sản phẩm", R.drawable.ic_purchasehistory, BeigeLight, "product_route"),
        HomeFeature("Đơn hàng", R.drawable.ic_order, BeigeLight, "order_list_route"),
        HomeFeature("Nhân viên", R.drawable.ic_staff, BeigeLight, "user_management_route", "QuanLy"),
        HomeFeature("Thống kê", R.drawable.ic_stats, BeigeLight, "statistics_route"),
        HomeFeature("Khuyến mại", R.drawable.ic_promo, BeigeLight, "promotion_management_route", "QuanLy")
    )

    // Logic lọc theo vai trò (Chỉ Quản lý mới thấy tất cả)
    val filteredFeatures = features.filter { feature ->
        feature.requiredRole == null || role == "QuanLy"
    }

    Scaffold(
        topBar = { TopBarContent() },
    bottomBar = { BottomTabBar(navController, homeRole = role) },
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SearchBarComponent()

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredFeatures) { feature ->
                    FeatureCard(feature = feature) {
                        navController.navigate(feature.route) // Điều hướng khi click
                    }
                }
            }
        }
    }
}

@Composable
fun TopBarContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 15.dp, end = 15.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo_cart),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(40.dp)
        )

        Text(
            text = "ONE FOOD",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RedPrimary
        )

        IconButton(onClick = { /* Mở Sidebar Menu */ }) {
            Icon(painterResource(id = R.drawable.ic_menu_hamburguer), contentDescription = "Menu", tint = Color.Black, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun SearchBarComponent() {
    OutlinedTextField(
        value = "",
        onValueChange = { /* Xử lý tìm kiếm */ },
        placeholder = { Text("Tìm kiếm") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            unfocusedContainerColor = Color(0xFFF0F0F0),
            focusedContainerColor = Color(0xFFF0F0F0),
            cursorColor = RedPrimary
        ),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    )
}

@Composable
fun FeatureCard(feature: HomeFeature, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = feature.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .height(130.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = feature.iconId),
                contentDescription = feature.title,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = feature.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    OneFoodTheme {
    HomeScreen(navController = rememberNavController(), role = "QuanLy")
    }
}