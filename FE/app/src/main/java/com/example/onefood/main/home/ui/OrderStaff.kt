package com.example.onefood.main.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.onefood.R
import com.example.onefood.data.model.OrderListItem
import com.example.onefood.main.home.viewmodel.OrderListViewModel
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderStaff(
    navController: NavController,
    viewModel: OrderListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Load orders when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadOrders(context)
    }
    
    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var isFilterExpanded by remember { mutableStateOf(false) }
    
    // Debounce search query (300ms)
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedSearchQuery = searchQuery
    }
    
    // Filter options
    val statusOptions = listOf("Hoàn thành", "Tiếp nhận")
    
    // Use derivedStateOf for filtered orders - only recomputes when dependencies change
    val filteredOrders by remember(orders, debouncedSearchQuery, selectedStatus) {
        derivedStateOf {
            orders.filter { order ->
                // Status filter
                val matchesStatus = selectedStatus == null || order.status == selectedStatus
                
                // Search filter
                val matchesSearch = if (debouncedSearchQuery.isBlank()) {
                    true
                } else {
                    val query = debouncedSearchQuery.trim().lowercase()
                    order.id.lowercase().contains(query) ||
                    order.staffName.lowercase().contains(query) ||
                    order.orderTime.lowercase().contains(query) ||
                    order.paymentMethod?.lowercase()?.contains(query) == true
                }
                
                matchesStatus && matchesSearch
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Đơn hàng", 
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Search bar and Status filter - Always visible at top
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm kiếm đơn hàng...") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Search, 
                                contentDescription = "Tìm kiếm",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            ) 
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            cursorColor = RedPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp, top = 10.dp, bottom = 10.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    )
                
                    // Filter button
                    Box(modifier = Modifier.wrapContentSize()) {
                        IconButton(
                            onClick = { isFilterExpanded = true },
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    color = if (selectedStatus != null) RedPrimary else Color(0xFFF5F5F5),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_filter),
                                contentDescription = "Lọc theo trạng thái",
                                tint = if (selectedStatus != null) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = isFilterExpanded,
                            onDismissRequest = { isFilterExpanded = false },
                            modifier = Modifier.widthIn(max = 200.dp)
                        ) {
                            // Option: All statuses
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Tất cả",
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedStatus == null) FontWeight.Medium else FontWeight.Normal
                                    ) 
                                },
                                onClick = {
                                    selectedStatus = null
                                    isFilterExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = if (selectedStatus == null) RedPrimary else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            
                            // Status options
                            statusOptions.forEach { status ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            status,
                                            fontSize = 14.sp,
                                            fontWeight = if (selectedStatus == status) FontWeight.Medium else FontWeight.Normal
                                        ) 
                                    },
                                    onClick = {
                                        selectedStatus = status
                                        isFilterExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (selectedStatus == status) RedPrimary else Color.Transparent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Show selected status badge
                if (selectedStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = RedPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable {
                                    selectedStatus = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedStatus!!,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = RedPrimary
                                )
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Xóa",
                                    tint = RedPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = RedPrimary)
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                                onClick = { viewModel.loadOrders(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                filteredOrders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (orders.isEmpty()) "Chưa có đơn hàng nào" else "Không tìm thấy đơn hàng",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    // Order List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredOrders,
                            key = { it.id }
                        ) { order ->
                            OrderListItemCard(
                                order = order,
                                onClick = {
                                    navController.navigate("order_detail_staff_route/${order.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderListItemCard(
    order: OrderListItem,
    onClick: () -> Unit
) {
    val statusColor = when (order.status) {
        "Hoàn thành" -> Color(0xFF4CAF50)
        "Tiếp nhận" -> Color(0xFFFF9800)
        else -> Color.Gray
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header với Order ID và Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.id,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                // Status Tag
                Box(
                    modifier = Modifier
                        .background(
                            statusColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.White, RoundedCornerShape(3.dp))
                        )
                        Text(
                            text = order.status,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Staff Information
            Text(
                text = "Nhân viên: ${order.staffName}",
                fontSize = 14.sp,
                color = Color.Black
            )
            
            // Order Date
            Text(
                text = "Thời gian: ${order.orderTime}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            // Payment Method
            if (!order.paymentMethod.isNullOrBlank() && order.paymentMethod != "Chưa thanh toán") {
                Text(
                    text = "Phương thức thanh toán: ${order.paymentMethod}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrderStaffPreview() {
    OneFoodTheme {
        OrderStaff(navController = rememberNavController())
    }
}
