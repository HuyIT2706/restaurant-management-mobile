package com.example.onefood.main.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onefood.R
import com.example.onefood.ui.theme.RedPrimary
import java.text.NumberFormat
import java.util.*

data class RevenueItem(
    val id: Int,
    val name: String,
    val category: String,
    val quantity: Int,
    val revenue: Int,
    val bestSeller: Boolean,
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueListScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }

    val items = remember {
        listOf(
            RevenueItem(1, "Lẩu 4 ngăn", "Lẩu", 4, 5_000_000, true, R.drawable.img_sp1),
            RevenueItem(2, "Sườn heo nướng mật ong", "Nướng", 2, 320_000, false, R.drawable.img_sp1),
            RevenueItem(3, "Cá hồi xông khói", "Món thêm", 4, 240_000, false, R.drawable.img_sp1)
        )
    }

    val filteredItems = items.filter { 
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.category.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thống kê", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm kiếm") },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = "Tìm",
                        tint = Color.Gray
                    ) 
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    focusedContainerColor = Color(0xFFF0F0F0),
                    cursorColor = RedPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Revenue List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredItems) { item ->
                    RevenueListItem(
                        item = item,
                        onClick = {
                            navController.navigate("revenue_detail/${item.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RevenueListItem(item: RevenueItem, onClick: () -> Unit) {
    val formattedRevenue = NumberFormat.getInstance(Locale("vi", "VN")).format(item.revenue)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Image
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0)),
                contentScale = ContentScale.Crop
            )

            // Product Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.bestSeller) {
                        Text(
                            text = "🔥 Bán chạy",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    text = item.category,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "SL: ${item.quantity}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            // Revenue
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$formattedRevenue đ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedPrimary
                )
            }
        }
    }
}

@Composable
fun RevenueDetailScreen(id: Int, navController: NavController) {
    var selectedDate by remember { mutableStateOf("29-09-2025") }
    val showDatePicker = remember { mutableStateOf(false) }

    val dish = RevenueItem(id, "Lẩu 4 ngăn", "Lẩu", 4, 5_000_000, true, R.drawable.img_sp1)
    val orders = listOf(
        Triple("DH49", "1", "22:09:26"),
        Triple("DH44", "1", "20:59:55"),
        Triple("DH43", "1", "15:03:12"),
        Triple("DH42", "1", "06:19:20")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, start = 16.dp, end = 16.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Chi tiết doanh thu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Product Image
                    Image(
                        painter = painterResource(id = dish.imageRes),
                        contentDescription = dish.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                item {
                    Column {
                        Text(
                            text = "Chi tiết món #$id",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = dish.name,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ngày: $selectedDate",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        IconButton(onClick = { showDatePicker.value = true }) {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = "Chọn ngày",
                                tint = Color.Black
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header Row
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Mã đơn", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("SL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Giá", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Thời gian", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            HorizontalDivider(color = Color(0xFFE0E0E0))

                            // Order Items
                            orders.forEach { order ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(order.first, fontSize = 14.sp, color = Color.Black)
                                    Text(order.second, fontSize = 14.sp, color = Color.Black)
                                    Text("430.000 đ", fontSize = 14.sp, color = Color.Black)
                                    Text(order.third, fontSize = 14.sp, color = Color.Black)
                                }
                                HorizontalDivider(color = Color(0xFFE0E0E0))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Total
                            Column {
                                Text(
                                    text = "Tổng SL: 4",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Tổng tiền: 1.660.000 VND",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RedPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker.value) {
        AlertDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker.value = false }) {
                    Text("OK", color = RedPrimary)
                }
            },
            title = { Text("Chọn ngày", fontWeight = FontWeight.Bold) },
            text = { Text("Tính năng chọn ngày sẽ được thêm sau.") }
        )
    }
}
