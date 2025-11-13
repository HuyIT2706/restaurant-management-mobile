package com.example.onefood.main.home.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.onefood.main.home.viewmodel.PromotionViewModel
import com.example.onefood.ui.theme.RedPrimary
import java.util.*

/** 🔹 Hàm format chỉ lấy ngày (YYYY-MM-DD → DD/MM/YYYY) */
fun formatDateOnly(datetime: String): String {
    return datetime.take(10).split("-").reversed().joinToString("/")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionEditScreen(
    navController: NavController,
    promotionId: Int,
    viewModel: PromotionViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // 🔹 Load thông tin khuyến mãi khi mở
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("onefood_prefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        if (token != null) {
            viewModel.loadPromotionById(token, promotionId)
        } else {
            Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    val promotion by viewModel.currentPromotion.collectAsState()

    if (promotion == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RedPrimary)
        }
        return
    }

    // 🔹 Biến dữ liệu
    var code by remember { mutableStateOf(promotion!!.code) }
    var quantity by remember { mutableStateOf(promotion!!.quantity.toString()) }
    var discountType by remember { mutableStateOf(promotion!!.discountType) }
    var showDiscountTypeDropdown by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(formatDateOnly(promotion!!.startDate)) }
    var endDate by remember { mutableStateOf(formatDateOnly(promotion!!.endDate)) }
    var discountValue by remember {
        mutableStateOf(promotion!!.discount.replace("%", "").replace(" đ", "").replace(".", "").trim())
    }
    var minOrderValue by remember {
        mutableStateOf(promotion!!.minOrderValue.replace(" đ", "").replace(".", "").trim())
    }
    var description by remember { mutableStateOf(promotion!!.description) }
    var isSubmitting by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()

    // 🔹 Chọn ngày từ DatePicker
    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    Dialog(
        onDismissRequest = { navController.popBackStack() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.85f),
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

                    // 🔹 Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "CẬP NHẬT KHUYẾN MÃI",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color.Black)
                            }
                        }
                    }

                    // 🔹 Mã khuyến mãi (readonly)
                    item {
                        OutlinedTextField(
                            value = code,
                            onValueChange = {},
                            label = { Text("Mã khuyến mãi") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // 🔹 Số lượng
                    item {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                            label = { Text("Số lượng áp dụng") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // 🔹 Loại giảm giá
                    item {
                        ExposedDropdownMenuBox(
                            expanded = showDiscountTypeDropdown,
                            onExpandedChange = { showDiscountTypeDropdown = !showDiscountTypeDropdown }
                        ) {
                            OutlinedTextField(
                                value = if (discountType == "PhanTram") "Phần trăm" else "Số tiền",
                                onValueChange = {},
                                label = { Text("Loại giảm giá") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDiscountTypeDropdown) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = showDiscountTypeDropdown,
                                onDismissRequest = { showDiscountTypeDropdown = false }
                            ) {
                                listOf("PhanTram", "SoTien").forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(if (type == "PhanTram") "Phần trăm" else "Số tiền") },
                                        onClick = {
                                            discountType = type
                                            showDiscountTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 🔹 Giá trị giảm
                    item {
                        OutlinedTextField(
                            value = discountValue,
                            onValueChange = { discountValue = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Giá trị giảm") },
                            suffix = { Text(if (discountType == "PhanTram") "%" else "đ") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // 🔹 Ngày bắt đầu & kết thúc
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = {},
                                label = { Text("Ngày bắt đầu") },
                                placeholder = { Text("DD/MM/YYYY") },
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Chọn ngày bắt đầu",
                                        tint = RedPrimary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { showDatePicker { selected -> startDate = selected } }
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showDatePicker { selected -> startDate = selected } },
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = endDate,
                                onValueChange = {},
                                label = { Text("Ngày kết thúc") },
                                placeholder = { Text("DD/MM/YYYY") },
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Chọn ngày kết thúc",
                                        tint = RedPrimary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { showDatePicker { selected -> endDate = selected } }
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showDatePicker { selected -> endDate = selected } },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // 🔹 Giá trị đơn hàng tối thiểu
                    item {
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

                    // 🔹 Mô tả
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Mô tả") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 3
                        )
                    }

                    // 🔹 Nút cập nhật
                    item {
                        Button(
                            onClick = {
                                isSubmitting = true
                                val prefs = context.getSharedPreferences("onefood_prefs", android.content.Context.MODE_PRIVATE)
                                val token = prefs.getString("jwt_token", null)
                                if (token == null) {
                                    Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
                                    isSubmitting = false
                                    return@Button
                                }

                                viewModel.updatePromotion(
                                    token = token,
                                    promoId = promotion!!.id,
                                    promoCode = code,
                                    promoType = discountType,
                                    promoValue = discountValue.toDoubleOrNull() ?: 0.0,
                                    promoQuantity = quantity.toIntOrNull() ?: 1,
                                    promoDesc = description,
                                    promoMinOrderAmount = minOrderValue.toDoubleOrNull() ?: 0.0,
                                    promoStartDate = startDate,
                                    promoEndDate = endDate,
                                    promoActive = if (promotion!!.status) 1 else 0
                                ) { success ->
                                    isSubmitting = false
                                    if (success) {
                                        Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(context, "Lỗi khi cập nhật khuyến mãi", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Cập nhật khuyến mãi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
