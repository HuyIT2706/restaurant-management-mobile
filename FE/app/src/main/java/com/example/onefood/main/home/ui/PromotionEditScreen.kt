package com.example.onefood.main.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.onefood.R
import com.example.onefood.ui.theme.RedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionEditScreen(
    navController: NavController,
    promotionId: Int
) {
    // Sample data - trong thực tế sẽ lấy từ ViewModel/Repository dựa trên promotionId
    // Để đơn giản, sử dụng sample data giống như trong PromotionListScreen
    val samplePromotions = remember {
        listOf(
            com.example.onefood.data.model.PromotionItem(1, "FLASH10", "01/09/2025", "15/09/2025", true, "10%", 5, "PhầnTrăm", "3.000.000 đ", "Giảm 10% cho đơn hàng FLASH10"),
            com.example.onefood.data.model.PromotionItem(2, "SALE50K", "10/09/2025", "20/09/2025", true, "50.000 đ", 10, "SốTiền", "1.000.000 đ", "Giảm 50.000 đ cho đơn hàng trên 1 triệu"),
            com.example.onefood.data.model.PromotionItem(3, "WEEKEND20", "15/09/2025", "30/09/2025", false, "20%", 3, "PhầnTrăm", "2.000.000 đ", "Giảm 20% cuối tuần")
        )
    }
    
    // Tìm promotion theo ID
    val promotion = remember(promotionId) {
        samplePromotions.find { it.id == promotionId } ?: samplePromotions.first()
    }
    
    var code by remember { mutableStateOf(promotion.code) }
    var quantity by remember { mutableStateOf(promotion.quantity.toString()) }
    var discountType by remember { mutableStateOf(promotion.discountType) }
    var showDiscountTypeDropdown by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(promotion.startDate) }
    var endDate by remember { mutableStateOf(promotion.endDate) }
    var status by remember { 
        mutableStateOf(
            if (promotion.status) "Hoạt động" else "Tạm dừng"
        )
    }
    var showStatusDropdown by remember { mutableStateOf(false) }
    
    // Extract discount value from discount string (e.g., "10%" -> "10" or "50000 đ" -> "50000")
    var discountValue by remember {
        mutableStateOf(
            promotion.discount.replace("%", "").replace(" đ", "").replace(".", "").trim()
        )
    }
    var minOrderValue by remember { 
        mutableStateOf(
            promotion.minOrderValue.replace(" đ", "").replace(".", "").trim()
        )
    }
    var description by remember { mutableStateOf(promotion.description) }

    Dialog(
        onDismissRequest = { navController.popBackStack() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { navController.popBackStack() }
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.85f)
                    .clickable { }, // Prevent clicks from passing through
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CẬP NHẬT KHUYẾN MÃI",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Đóng",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    item {
                        // Mã khuyến mãi (disabled)
                        OutlinedTextField(
                            value = code,
                            onValueChange = { },
                            label = { Text("Mã khuyến mãi") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            enabled = false
                        )
                    }

                    item {
                        // Số lượng áp dụng
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                            label = { Text("Số lượng áp dụng") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    item {
                        // Loại giảm giá
                        ExposedDropdownMenuBox(
                            expanded = showDiscountTypeDropdown,
                            onExpandedChange = { showDiscountTypeDropdown = !showDiscountTypeDropdown }
                        ) {
                            OutlinedTextField(
                                value = if (discountType == "PhầnTrăm") "Phần trăm" else "Số tiền",
                                onValueChange = { },
                                label = { Text("Loại giảm giá") },
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDiscountTypeDropdown)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = showDiscountTypeDropdown,
                                onDismissRequest = { showDiscountTypeDropdown = false }
                            ) {
                                listOf("PhầnTrăm", "SốTiền").forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(if (type == "PhầnTrăm") "Phần trăm" else "Số tiền") },
                                        onClick = {
                                            discountType = type
                                            showDiscountTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Giá trị giảm
                        OutlinedTextField(
                            value = discountValue,
                            onValueChange = {
                                discountValue = if (discountType == "PhầnTrăm") {
                                    it.filter { c -> c.isDigit() }.take(3)
                                } else {
                                    it.filter { c -> c.isDigit() || c == '.' }
                                }
                            },
                            label = { Text("Nhập giá trị giảm giá") },
                            placeholder = { Text(if (discountType == "PhầnTrăm") "VD: 10" else "VD: 50000") },
                            suffix = { Text(if (discountType == "PhầnTrăm") "%" else "đ") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    item {
                        // Ngày bắt đầu và kết thúc
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = { Text("Ngày bắt đầu") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = { Text("Ngày kết thúc") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    item {
                        // Giá trị đơn hàng tối thiểu
                        OutlinedTextField(
                            value = minOrderValue,
                            onValueChange = { minOrderValue = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Giá trị đơn hàng tối thiểu") },
                            suffix = { Text("đ") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    item {
                        // Trạng thái
                        ExposedDropdownMenuBox(
                            expanded = showStatusDropdown,
                            onExpandedChange = { showStatusDropdown = !showStatusDropdown }
                        ) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = { },
                                label = { Text("Trạng thái") },
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusDropdown)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = showStatusDropdown,
                                onDismissRequest = { showStatusDropdown = false }
                            ) {
                                listOf("Hoạt động", "Tạm dừng", "Kết thúc").forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s) },
                                        onClick = {
                                            status = s
                                            showStatusDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Mô tả
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Nhập mô tả") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 3
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        // Submit Button
                        Button(
                            onClick = {
                                // Handle update promotion
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_update),
                                contentDescription = "Cập nhật",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cập nhật khuyến mãi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
