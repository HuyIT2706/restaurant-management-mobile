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
fun PromotionAddScreen(navController: NavController) {
    var code by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("PhầnTrăm") }
    var showDiscountTypeDropdown by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Hoạt động") }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var discountValue by remember { mutableStateOf("") }
    var minOrderValue by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

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
                                text = "THÊM KHUYẾN MÃI",
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
                        // Mã khuyến mãi
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Mã khuyến mãi") },
                            placeholder = { Text("VD: FLASH10") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    item {
                        // Số lượng áp dụng
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                            label = { Text("Số lượng áp dụng") },
                            placeholder = { Text("VD: 10") },
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
                                            discountValue = ""
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
                                placeholder = { Text("DD/MM/YYYY") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = { Text("Ngày kết thúc") },
                                placeholder = { Text("DD/MM/YYYY") },
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
                            placeholder = { Text("VD: 1000000") },
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
                            placeholder = { Text("VD: Giảm 10% cho đơn hàng FLASH10") },
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
                                // Handle add promotion
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Thêm khuyến mãi",
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
