package com.example.onefood.main.home.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.util.Calendar

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PromotionEditScreen(
    navController: NavController,
    promotionId: Int,
    viewModel: PromotionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(promotionId) {
        val prefs = context.getSharedPreferences("onefood_prefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        if (token != null) {
            authToken = token
            viewModel.loadPromotionById(token, promotionId)
        } else {
            Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    val promotionState by viewModel.currentPromotion.collectAsState()
    val detailLoading by viewModel.promotionDetailLoading.collectAsState()
    val detailError by viewModel.promotionDetailError.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.clearPromotionDetail() }
    }

    if (promotionState == null) {
        if (detailLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RedPrimary)
            }
        } else {
            AlertDialog(
                onDismissRequest = { navController.popBackStack() },
                confirmButton = {
                    TextButton(onClick = { authToken?.let { viewModel.loadPromotionById(it, promotionId) } }) {
                        Text("Thử lại")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Đóng")
                    }
                },
                title = { Text("Lỗi tải dữ liệu") },
                text = { Text(detailError ?: "Không thể tải thông tin khuyến mãi.") }
            )
        }
        return
    }

    val promotion = promotionState!!

    var code by remember { mutableStateOf(promotion.code ?: "") }
    var quantity by remember { mutableStateOf((promotion.quantity ?: 0).toString()) }
    var discountType by remember { mutableStateOf(canonicalPromotionType(promotion.discountType)) }
    var showDiscountTypeDropdown by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(formatDateOnly(promotion.startDate)) }
    var endDate by remember { mutableStateOf(formatDateOnly(promotion.endDate)) }
    var discountValue by remember { mutableStateOf(extractPromotionNumericString(promotion.discount)) }
    var minOrderValue by remember { mutableStateOf(extractPromotionNumericString(promotion.minOrderValue)) }
    var description by remember { mutableStateOf(promotion.description ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()

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
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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

                    item {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Số lượng áp dụng") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

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
                                DropdownMenuItem(
                                    text = { Text("Phần trăm") },
                                    onClick = {
                                        discountType = "PhanTram"
                                        showDiscountTypeDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Số tiền") },
                                    onClick = {
                                        discountType = "SoTien"
                                        showDiscountTypeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = discountValue,
                            onValueChange = { discountValue = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Giá trị giảm") },
                            suffix = { Text(if (discountType == "PhanTram") "%" else "đ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

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

                    item {
                        OutlinedTextField(
                            value = minOrderValue,
                            onValueChange = { minOrderValue = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Giá trị đơn hàng tối thiểu") },
                            suffix = { Text("đ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

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

                    item {
                        Button(
                            onClick = {
                                isSubmitting = true
                                val token = authToken
                                if (token == null) {
                                    Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
                                    isSubmitting = false
                                    return@Button
                                }

                                viewModel.updatePromotion(
                                    token = token,
                                    promoId = promotion.id ?: promotionId,
                                    promoCode = code,
                                    promoType = discountType,
                                    promoValue = discountValue.toDoubleOrNull() ?: 0.0,
                                    promoQuantity = quantity.toIntOrNull() ?: 1,
                                    promoDesc = description,
                                    promoMinOrderAmount = minOrderValue.toDoubleOrNull() ?: 0.0,
                                    promoStartDate = startDate,
                                    promoEndDate = endDate,
                                    promoActive = if (promotion.status == true) 1 else 0
                                ) { success ->
                                    isSubmitting = false
                                    if (success) {
                                        Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                        authToken?.let { viewModel.loadPromotionById(it, promotionId) }
                                        navController.previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("promotion_refresh", true)
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
                                Text(
                                    "Cập nhật khuyến mãi",
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
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PromotionDetailScreen(
    navController: NavController,
    promotionId: Int,
    viewModel: PromotionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(promotionId) {
        val prefs = context.getSharedPreferences("onefood_prefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        if (token != null) {
            authToken = token
            viewModel.loadPromotionById(token, promotionId)
        } else {
            Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    val promotionState by viewModel.currentPromotion.collectAsState()
    val detailLoading by viewModel.promotionDetailLoading.collectAsState()
    val detailError by viewModel.promotionDetailError.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.clearPromotionDetail() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chi tiết khuyến mãi", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        when {
            detailLoading && promotionState == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RedPrimary)
                }
            }

            !detailLoading && promotionState == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = detailError ?: "Không tìm thấy khuyến mãi.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                    Button(onClick = { authToken?.let { viewModel.loadPromotionById(it, promotionId) } }) {
                        Text("Thử lại")
                    }
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Đóng")
                    }
                }
            }

            promotionState != null -> {
                val promotion = promotionState!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailRow("Mã khuyến mãi", promotion.code ?: "-")
                            DetailRow("Loại giảm", formatPromotionTypeLabel(promotion.discountType))
                            DetailRow("Giá trị giảm", formatPromotionDiscount(promotion), valueColor = Color(0xFFE65100))
                            DetailRow("Số lượng còn", (promotion.quantity ?: 0).toString())
                            DetailRow("Ngày bắt đầu", formatPromotionDate(promotion.startDate))
                            DetailRow("Ngày kết thúc", formatPromotionDate(promotion.endDate))
                            DetailRow("Đơn hàng tối thiểu", formatPromotionMinOrder(promotion))
                            DetailRow(
                                label = "Trạng thái",
                                value = promotionStatusLabel(promotion),
                                valueColor = promotionStatusColor(promotion)
                            )
                        }
                    }

                    if (!promotion.description.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7E6)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Mô tả", fontWeight = FontWeight.SemiBold, color = Color(0xFFE65100))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(promotion.description ?: "-", color = Color.Black)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            promotion.id?.let { navController.navigate("promotion_edit/$it") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Chỉnh sửa khuyến mãi", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

private fun formatDateOnly(datetime: String?): String {
    if (datetime.isNullOrBlank()) return ""
    return datetime.take(10).split("-").reversed().joinToString("/")
}
