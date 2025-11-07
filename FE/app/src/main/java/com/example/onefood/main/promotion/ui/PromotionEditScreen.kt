package com.example.onefood.main.promotion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionEditScreen(navController: NavController, id: Int) {
    var code by remember { mutableStateOf("FLASH10") }
    var quantity by remember { mutableStateOf("5") }
    var discountType by remember { mutableStateOf("PhầnTrăm") }
    var startDate by remember { mutableStateOf("01/09/2025") }
    var endDate by remember { mutableStateOf("15/09/2025") }
    var status by remember { mutableStateOf("Hoạt động") }
    var discountValue by remember { mutableStateOf("10") }
    var minOrderValue by remember { mutableStateOf("3.000.000") }
    var description by remember { mutableStateOf("Giảm 10% cho đơn hàng FLASH10") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cập nhật khuyến mãi", fontSize = 20.sp) },
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
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mã khuyến mãi
            OutlinedTextField(
                value = code,
                onValueChange = { },
                label = { Text("Mã khuyến mãi") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )

            // Số lượng áp dụng
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                label = { Text("Số lượng áp dụng") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Loại giảm giá
            var expandedType by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
                OutlinedTextField(
                    value = if (discountType == "PhầnTrăm") "PhầnTrăm" else "SốTiền",
                    onValueChange = { },
                    label = { Text("Loại giảm giá") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                    listOf("PhầnTrăm", "SốTiền").forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                discountType = type
                                expandedType = false
                                discountValue = if (type == "PhầnTrăm") "10" else "50.000"
                            }
                        )
                    }
                }
            }

            // NGÀY BẮT ĐẦU & KẾT THÚC – TÁCH RIÊNG LABEL
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Ngày bắt đầu") },
                    placeholder = { Text("01/09/2025") },
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("Ngày kết thúc") },
                    placeholder = { Text("15/09/2025") },
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )
            }

            // Trạng thái
            var expandedStatus by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = !expandedStatus }) {
                OutlinedTextField(
                    value = status,
                    onValueChange = { },
                    label = { Text("Trạng thái") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                    listOf("Hoạt động", "Tạm dừng", "Kết thúc").forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = { status = s; expandedStatus = false }
                        )
                    }
                }
            }

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
                placeholder = { Text(if (discountType == "PhầnTrăm") "VD: 10" else "VD: 50.000") },
                suffix = { Text(if (discountType == "PhầnTrăm") "%" else "đ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Giá trị đơn hàng tối thiểu
            OutlinedTextField(
                value = minOrderValue,
                onValueChange = { minOrderValue = it.filter { c -> c.isDigit() } },
                label = { Text("Giá trị đơn hàng tối thiểu") },
                prefix = { Text("VD: ") },
                suffix = { Text("đ") },
                placeholder = { Text("3.000.000") },
                modifier = Modifier.fillMaxWidth()
            )

            // Mô tả
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Nhập mô tả") },
                placeholder = { Text("VD: Giảm 10% cho đơn hàng FLASH10") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Cập nhật khuyến mãi", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}