package com.example.onefood.main.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.onefood.main.home.viewmodel.OrderDetailViewModel
import com.example.onefood.main.home.viewmodel.PaymentState
import com.example.onefood.main.home.viewmodel.PaymentViewModel
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import com.example.onefood.util.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    orderId: String,
    orderDetailViewModel: OrderDetailViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val orderDetail by orderDetailViewModel.orderDetail.collectAsState()
    val isLoading by orderDetailViewModel.isLoading.collectAsState()
    val error by orderDetailViewModel.error.collectAsState()
    
    val paymentState by paymentViewModel.paymentState.collectAsState()
    val vnpayUrl by paymentViewModel.vnpayUrl.collectAsState()
    val promotionCode by paymentViewModel.promotionCode.collectAsState()
    val selectedPaymentMethod by paymentViewModel.selectedPaymentMethod.collectAsState()
    val isPaid by paymentViewModel.isPaid.collectAsState()
    
    // Load order detail when screen is first displayed
    LaunchedEffect(orderId) {
        orderDetailViewModel.loadOrderDetail(context, orderId)
    }
    
    // Calculate totals
    val totalAmount = orderDetail?.totalAmount ?: 0
    val discountAmount = paymentViewModel.calculateDiscount(totalAmount, promotionCode)
    val finalAmount = totalAmount - discountAmount
    
    // Handle payment state changes
    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is PaymentState.Success -> {
                // Navigate back to order detail after successful payment
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is PaymentState.Error -> {
                // Show error message
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    // Open VNPay URL when available
    LaunchedEffect(vnpayUrl) {
        val url = vnpayUrl
        if (!url.isNullOrBlank()) {
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                context.startActivity(intent)
                // Reset url after launching
                paymentViewModel.clearVnPayUrl()
            } catch (_: Exception) { }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "THANH TOÁN", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    navigationIconContentColor = Color.Black,
                    titleContentColor = Color.Black
                )
            )
        },
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RedPrimary)
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = error!!,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = { orderDetailViewModel.loadOrderDetail(context, orderId) },
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            orderDetail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy đơn hàng",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Order Status and Table Information
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bàn số: ${orderDetail!!.tableNumber}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = if (isPaid) "Đã thanh toán" else "Đã tiếp nhận",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isPaid) Color(0xFF4CAF50) else Color(0xFF4CAF50)
                            )
                        }
                    }
                    
                    // Order Items Table Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tên sản phẩm",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "Số lượng",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = "Giá",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                            Text(
                                text = "Tổng tiền",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }
                    
                    // Order Items List
                    items(
                        items = orderDetail!!.orderItems,
                        key = { item -> "${item.productName}_${item.quantity}_${item.price}" }
                    ) { item ->
                        PaymentItemRow(item = item)
                    }
                    
                    // Divider
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                    
                    // Order Summary
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Total",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Bàn : ${orderDetail!!.tableNumber}",
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tổng tiền:",
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${totalAmount.formatPrice().replace(" ₫", "")} VND",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                            
                            // Promotion Code Input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Khuyến mãi:",
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                                OutlinedTextField(
                                    value = promotionCode,
                                    onValueChange = { paymentViewModel.updatePromotionCode(it) },
                                    placeholder = { Text("Nhập mã khuyến mãi") },
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFE0E0E0),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                                )
                            }
                            
                            // Final Amount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Thành tiền:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${finalAmount.formatPrice().replace(" ₫", "")} VND",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                    
                    // Payment Method Selection
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Chọn phương thức thanh toán:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            PaymentMethodDropdown(
                                selectedMethod = selectedPaymentMethod,
                                onMethodSelected = { paymentViewModel.updatePaymentMethod(it) }
                            )
                        }
                    }
                    
                    // Payment Button (Thanh toán)
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (selectedPaymentMethod == "Ngân hàng") {
                                    paymentViewModel.createVnPayUrl(context, orderId)
                                } else {
                                    paymentViewModel.processPayment(context, orderId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RedPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Thanh toán",
                                fontSize = 18.sp,
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

@Composable
fun PaymentItemRow(
    item: com.example.onefood.data.model.OrderDetailItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.productName ?: "",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = "${item.quantityInt}",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = "${item.priceInt.formatPrice().replace(" ₫", "")} VND",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
        Text(
            text = "${item.totalPrice.formatPrice().replace(" ₫", "")} VND",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodDropdown(
    selectedMethod: String,
    onMethodSelected: (String) -> Unit
) {
    val paymentMethods = listOf("Ngân hàng", "Tiền mặt")
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedMethod,
            onValueChange = { },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE0E0E0),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            paymentMethods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method) },
                    onClick = {
                        onMethodSelected(method)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    OneFoodTheme {
        PaymentScreen(
            navController = rememberNavController(),
            orderId = "6"
        )
    }
}
