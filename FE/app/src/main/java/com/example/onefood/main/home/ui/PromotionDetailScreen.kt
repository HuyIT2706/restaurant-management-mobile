package com.example.onefood.main.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onefood.data.model.PromotionItem
import com.example.onefood.ui.theme.RedPrimary

@Composable
fun PromotionDetailScreen(navController: NavController, id: Int) {
    // Sample data - sau này sẽ lấy từ ViewModel/API dựa trên id
    val promotion = remember(id) {
        PromotionItem(
            id = id,
            code = "FLASH10",
            startDate = "01/09/2025",
            endDate = "15/09/2025",
            status = true,
            discount = "10%",
            quantity = 5,
            discountType = "PhầnTrăm",
            minOrderValue = "3.000.000 đ",
            description = "Giảm 10% cho đơn hàng FLASH10"
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header với back button
            item {
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
                        text = "Chi tiết khuyến mãi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // Main Content Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Section Title
                        Text(
                            text = "CHI TIẾT KHUYẾN MÃI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                        // Promotion Information
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PromotionInfoRow("Mã khuyến mãi", promotion.code)
                            PromotionInfoRow("Số lượng còn lại", "${promotion.quantity}")
                            PromotionInfoRow("Loại giảm giá", if (promotion.discountType == "PhầnTrăm") "Phần trăm" else "Số tiền")
                            PromotionInfoRow("Giá trị giảm", promotion.discount)
                            PromotionInfoRow("Giá trị đơn hàng tối thiểu", promotion.minOrderValue)
                            PromotionInfoRow("Ngày bắt đầu", promotion.startDate)
                            PromotionInfoRow("Ngày kết thúc", promotion.endDate)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Trạng thái",
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (promotion.status) Color(0xFF4CAF50) else Color.Red,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (promotion.status) "Hoạt động" else "Không hoạt động",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                            if (promotion.description.isNotEmpty()) {
                                PromotionInfoRow("Mô tả", promotion.description)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromotionInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}
