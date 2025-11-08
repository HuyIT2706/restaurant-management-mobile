package com.example.onefood.main.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import java.text.NumberFormat
import java.util.*
import com.example.onefood.data.models.RevenueItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueListScreen(navController: NavController) {
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Tất cả") }
    var showFilter by remember { mutableStateOf(false) }

    val categories = listOf("Tất cả", "Lẩu", "Nướng", "Hải sản", "Món thêm", "Khai vị", "Cơm", "Xào", "Nước")

    val items = listOf(
        RevenueItem(1, "Lẩu 4 ngăn", "Lẩu", 4, 5_000_000, true, R.drawable.lau),
        RevenueItem(2, "Sườn heo nướng mật ong", "Nướng", 2, 320_000, false, R.drawable.suon),
        RevenueItem(3, "Cá hồi xông khói", "Món thêm", 4, 240_000, false, R.drawable.cahoi)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doanh thu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1C1E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1C1C1E)
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(12.dp)
        ) {

            // 🔍 Thanh tìm kiếm + tải lại + lọc
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = {
                        Text(
                            "Tìm món theo tên",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.Black
                    ),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )

                Spacer(Modifier.width(6.dp))

                IconButton(
                    onClick = { search = "" },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = Color(0xFF444444))
                }

                Spacer(Modifier.width(6.dp))

                Box {
                    IconButton(
                        onClick = { showFilter = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, shape = CircleShape)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Lọc", tint = Color(0xFF444444))
                    }
                    DropdownMenu(
                        expanded = showFilter,
                        onDismissRequest = { showFilter = false }
                    ) {
                        categories.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    filter = it
                                    showFilter = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // 📋 Danh sách món doanh thu
            LazyColumn {
                items(items.filter {
                    (filter == "Tất cả" || it.category == filter) &&
                            it.name.contains(search, ignoreCase = true)
                }) { dish ->
                    RevenueListItem(dish) {
                        navController.navigate("revenue_detail/${dish.id}")
                    }
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
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.bestSeller) {
                        Spacer(Modifier.width(6.dp))
                        Text("🔥 Bán chạy", color = Color(0xFF4CAF50), fontSize = 12.sp)
                    }
                }
                Text(item.category, color = Color.Gray, fontSize = 12.sp)
                Text("SL: ${item.quantity}", color = Color.Gray, fontSize = 12.sp)
            }

            Text(
                "$formattedRevenue đ",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueDetailScreen(id: Int, navController: NavController) {
    var selectedDate by remember { mutableStateOf("29-09-2025") }
    val showDatePicker = remember { mutableStateOf(false) }

    val dish = RevenueItem(id, "Lẩu 4 ngăn", "Lẩu", 4, 5_000_000, true, R.drawable.lau)
    val orders = listOf(
        Triple("DH49", "1", "22:09:26"),
        Triple("DH44", "1", "20:59:55"),
        Triple("DH43", "1", "15:03:12"),
        Triple("DH42", "1", "06:19:20")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết doanh thu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = dish.imageRes),
                contentDescription = dish.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(12.dp))
            Text("Chi tiết món #$id", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(dish.name, color = Color.Gray)

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ngày: $selectedDate", color = Color.Gray)
                IconButton(onClick = { showDatePicker.value = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Chọn ngày")
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mã đơn", fontWeight = FontWeight.Bold)
                Text("SL", fontWeight = FontWeight.Bold)
                Text("Giá", fontWeight = FontWeight.Bold)
                Text("Thời gian", fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()

            orders.forEach {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(it.first)
                    Text(it.second)
                    Text("430.000 đ")
                    Text(it.third)
                }
                HorizontalDivider()
            }

            Spacer(Modifier.height(12.dp))
            Text("Tổng SL: 4", fontWeight = FontWeight.Bold)
            Text(
                "Tổng tiền: 1.660.000 VND",
                color = Color(0xFFEE6C4D),
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showDatePicker.value) {
        AlertDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker.value = false }) {
                    Text("OK")
                }
            },
            title = { Text("Chọn ngày") },
            text = { Text("Tính năng chọn ngày sẽ được thêm sau.") }
        )
    }
}
