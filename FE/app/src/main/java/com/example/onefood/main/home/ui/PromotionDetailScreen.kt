package com.example.onefood.main.home.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onefood.main.home.viewmodel.PromotionViewModel
import com.example.onefood.ui.theme.RedPrimary
import androidx.compose.material.icons.Icons

// ✅ Hàm định dạng ngày (yyyy-MM-dd -> dd-MM-yyyy)
fun formatDateDisplay(date: String): String {
    val parts = date.take(10).split("-")
    return if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else date
}

@Composable
fun PromotionDetailScreen(
    navController: NavController,
    id: Int,
    viewModel: PromotionViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current

    // ✅ Lấy token và load dữ liệu
    LaunchedEffect(id) {
        val prefs = context.getSharedPreferences("onefood_prefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        if (token != null) {
            viewModel.loadPromotionById(token, id)
        } else {
            Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    val promotion by viewModel.currentPromotion.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) { paddingValues ->
        when {
            promotion == null -> {
                // Hiển thị loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RedPrimary)
                }
            }

            else -> {
                val promo = promotion!!
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Header
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
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

                    // Nội dung chính
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
                                Text(
                                    text = "CHI TIẾT KHUYẾN MÃI",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                HorizontalDivider(
                                    color = Color(0xFFE0E0E0),
                                    thickness = 1.dp
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    PromotionInfoRow("Mã khuyến mãi", promo.code)
                                    PromotionInfoRow("Số lượng còn lại", "${promo.quantity}")
                                    PromotionInfoRow(
                                        "Loại giảm giá",
                                        if (promo.discountType == "PhanTram") "Phần trăm" else "Số tiền"
                                    )
                                    PromotionInfoRow("Giá trị giảm", promo.discount)
                                    PromotionInfoRow("Giá trị đơn hàng tối thiểu", promo.minOrderValue)

                                    // ✅ Hiển thị ngày đã format dd-MM-yyyy
                                    PromotionInfoRow("Ngày bắt đầu", formatDateDisplay(promo.startDate))
                                    PromotionInfoRow("Ngày kết thúc", formatDateDisplay(promo.endDate))

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
                                                    if (promo.status) Color(0xFF4CAF50) else Color.Red,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (promo.status) "Hoạt động" else "Không hoạt động",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    if (promo.description.isNotEmpty()) {
                                        PromotionInfoRow("Mô tả", promo.description)
                                    }
                                }
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
