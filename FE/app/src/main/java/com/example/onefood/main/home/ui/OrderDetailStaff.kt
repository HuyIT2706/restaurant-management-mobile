package com.example.onefood.main.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.onefood.R
import com.example.onefood.data.model.OrderDetailItem
import com.example.onefood.main.home.viewmodel.OrderDetailViewModel
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import com.example.onefood.util.formatPrice
import android.widget.Toast
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailStaff(
    navController: NavController,
    orderId: String,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val orderDetail by viewModel.orderDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Determine if order is paid based on order status or payment method
    // Default button shows "Chưa thanh toán" until nhân viên xác nhận thủ công
    var currentPaymentStatus by remember { mutableStateOf(false) }
    
    // Load order detail when screen is first displayed or when orderId changes
    // This will also trigger when screen is recreated after navigation from deep link
    LaunchedEffect(orderId) {
        viewModel.loadOrderDetail(context, orderId)
    }
    
    // Handle payment status toggle
    val onTogglePaymentStatus = {
        currentPaymentStatus = !currentPaymentStatus
        // TODO: Call API to update payment status on server
        // For now, just update local state
        if (currentPaymentStatus) {
            Toast.makeText(context, "Đơn hàng đã được đánh dấu là đã thanh toán", Toast.LENGTH_SHORT).show()
            // Refresh order detail to get updated status
            viewModel.refreshOrderDetail(context, orderId)
        } else {
            Toast.makeText(context, "Đơn hàng đã được đánh dấu là chưa thanh toán", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Chi tiết đơn hàng", 
                        fontWeight = FontWeight.Medium,
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
                            onClick = { viewModel.loadOrderDetail(context, orderId) },
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
                    // Order Items Section
                    item {
                        Text(
                            text = "Danh sách món",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    
                    // Order Items List using LazyColumn items
                    items(
                        items = orderDetail!!.orderItems,
                        key = { item -> "${item.productName}_${item.notes}_${item.quantity}" }
                    ) { item ->
                        OrderDetailItemCard(item = item)
                    }
                    
                    // Order Information Section
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                    
                    item {
                        Text(
                            text = "Thông tin đơn hàng",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    
                    item {
                        OrderInfoRow("Mã đơn hàng", "DH " + orderDetail!!.id)
                    }
                    
                    item {
                        OrderInfoRow("Nhân viên Order", orderDetail!!.staffName)
                    }
                    
                    if (orderDetail!!.cashier.isNotEmpty()) {
                        item {
                            OrderInfoRow("Thu ngân", orderDetail!!.cashier)
                        }
                    }
                    
                    item {
                        OrderInfoRow("Thời gian đặt", orderDetail!!.orderTime)
                    }
                    
                    if (orderDetail!!.paymentTime.isNotEmpty()) {
                        item {
                            OrderInfoRow("Thời gian thanh toán", orderDetail!!.paymentTime)
                        }
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trạng thái",
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Text(
                                text = orderDetail!!.status,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (orderDetail!!.status.lowercase()) {
                                    "hoàn thành", "completed" -> Color(0xFF4CAF50)
                                    "tiếp nhận", "pending" -> Color(0xFFFF9800)
                                    else -> Color(0xFF2196F3)
                                }
                            )
                        }
                    }
                    
                    // Table Number Section
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                    
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Bàn",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFFF5F5F5),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = orderDetail!!.tableNumber,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                    
                    // Total Amount Section
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                    
                    // Thành tiền Section (above buttons)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Thành tiền",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = orderDetail!!.totalAmount.formatPrice(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RedPrimary
                                )
                            }
                        }
                    }
                    
                    // Bottom Action Bar: Two buttons in a row
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Thanh toán button (navigate to payment screen)
                            Button(
                                onClick = {
                                    navController.navigate("payment_route/${orderDetail!!.id}")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Thanh toán",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            
                            // Right: Chưa thanh toán / Đã thanh toán button (toggle payment status)
                            Button(
                                onClick = onTogglePaymentStatus,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentPaymentStatus) Color(0xFF4CAF50) else RedPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (currentPaymentStatus) "Đã thanh toán" else "Chưa thanh toán",
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

@Composable
fun OrderDetailItemCard(
    item: OrderDetailItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            val imageUrl = item.getDisplayImageUrl()
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.img_productwhite),
                        error = painterResource(id = R.drawable.img_productwhite)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_productwhite),
                        contentDescription = "No image",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Product Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.productName ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "SL: ${item.quantityInt}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                if (!item.notes.isNullOrBlank()) {
                    Text(
                        text = "Ghi chú: ${item.notes}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
            
            // Price
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = item.priceInt.formatPrice(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = item.totalPrice.formatPrice(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedPrimary
                )
            }
        }
    }
}

@Composable
fun OrderInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrderDetailStaffPreview() {
    OneFoodTheme {
        OrderDetailStaff(
            navController = rememberNavController(),
            orderId = "DH52"
        )
    }
}
