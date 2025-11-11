package com.example.onefood.main.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.onefood.data.model.CartItem
import com.example.onefood.data.model.OrderItemRequest
import com.example.onefood.core.components.BottomTabBar
import com.example.onefood.main.home.viewmodel.CartViewModel
import com.example.onefood.main.home.viewmodel.CreateOrderViewModel
import com.example.onefood.main.home.viewmodel.CreateOrderState
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import com.example.onefood.util.formatPrice

// Import rememberCartViewModel from CartScreen
import com.example.onefood.main.home.ui.rememberCartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableOrderDetailScreen(
    navController: NavController,
    tableId: Int,
    cartViewModel: CartViewModel = rememberCartViewModel(),
    createOrderViewModel: CreateOrderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }
    
    // Debounce search query (300ms)
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedSearchQuery = searchQuery
    }
    
    // Observe create order state
    val createOrderState by createOrderViewModel.state.collectAsState()
    
    // Get cart items for this table from ViewModel
    val cartItemsState by cartViewModel.cartItems.collectAsState()
    val cartItems = remember(cartItemsState, tableId) {
        cartViewModel.getCartItemsForTable(tableId)
    }
    
    // Use derivedStateOf for filtered items - only recomputes when dependencies change
    val filteredItems by remember(cartItems, debouncedSearchQuery) {
        derivedStateOf {
            if (debouncedSearchQuery.isBlank()) {
                cartItems
            } else {
                cartItems.filter { 
                    it.productName.contains(debouncedSearchQuery, ignoreCase = true) ||
                    it.category.contains(debouncedSearchQuery, ignoreCase = true) ||
                    it.notes.contains(debouncedSearchQuery, ignoreCase = true)
                }
            }
        }
    }
    
    // Use derivedStateOf for total amount - only recomputes when cartItems change
    val totalAmount by remember(cartItems) {
        derivedStateOf {
            cartItems.sumOf { it.totalPrice }
        }
    }
    
    // Handle create order state changes
    LaunchedEffect(createOrderState) {
        when (val state = createOrderState) {
            is CreateOrderState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                // Clear cart for this table after successful order
                cartViewModel.clearCartForTable(tableId)
                // Reset state
                createOrderViewModel.resetState()
                // Navigate back to cart screen
                navController.popBackStack()
            }
            is CreateOrderState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                createOrderViewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bàn $tableId", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomTabBar(navController = navController)
        },
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = "Tìm",
                            tint = Color.Gray
                        ) 
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedContainerColor = Color(0xFFF0F0F0),
                        cursorColor = RedPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    singleLine = true
                )
                
                Spacer(Modifier.height(16.dp))
                
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Giỏ hàng trống",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = "Giỏ hàng trống",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    // Cart Items List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(
                            items = filteredItems,
                            key = { "${it.productId}_${it.notes}" }
                        ) { item ->
                            CartItemCard(
                                item = item,
                                onQuantityIncrease = {
                                    cartViewModel.updateItemQuantity(
                                        tableId = tableId,
                                        itemId = item.productId,
                                        notes = item.notes,
                                        newQuantity = item.quantity + 1
                                    )
                                },
                                onQuantityDecrease = {
                                    // Chỉ giảm nếu quantity > 1
                                    if (item.quantity > 1) {
                                        cartViewModel.updateItemQuantity(
                                            tableId = tableId,
                                            itemId = item.productId,
                                            notes = item.notes,
                                            newQuantity = item.quantity - 1
                                        )
                                    }
                                },
                                onRemove = {
                                    cartViewModel.removeItemFromCart(
                                        tableId = tableId,
                                        itemId = item.productId,
                                        notes = item.notes
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            // Total amount và action buttons - Fixed at bottom
            if (cartItems.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.White)
                        .padding(16.dp)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tổng tiền:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = totalAmount.formatPrice(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Two buttons: Thêm món and Đặt hàng
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Thêm món button
                        OutlinedButton(
                            onClick = {
                                navController.navigate("order_menu_route/$tableId")
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Thêm món",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        // Đặt hàng button
                        val isCreatingOrder = createOrderState is CreateOrderState.Loading
                        Button(
                            onClick = { 
                                // Convert cartItems to OrderItemRequest list
                                val orderItems = cartItems.map { cartItem ->
                                    OrderItemRequest(
                                        productId = cartItem.productId,
                                        quantity = cartItem.quantity,
                                        price = cartItem.price.toDouble(),
                                        notes = cartItem.notes
                                    )
                                }
                                
                                // Call create order API
                                createOrderViewModel.createOrder(
                                    context = context,
                                    tableId = tableId,
                                    items = orderItems
                                )
                            },
                            enabled = !isCreatingOrder && cartItems.isNotEmpty(),
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isCreatingOrder) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Đặt hàng",
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
fun CartItemCard(
    item: CartItem,
    onQuantityIncrease: () -> Unit,
    onQuantityDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    // Cache calculations to avoid recomputation
    val canDecrease = remember(item.quantity) { item.quantity > 1 }
    val formattedPrice = remember(item.price) { item.price.formatPrice() }
    val hasNotes = remember(item.notes) { item.notes.isNotEmpty() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image - Optimized with fixed size
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl != null && item.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.productName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.img_productwhite),
                        error = painterResource(id = R.drawable.img_productwhite)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_productwhite),
                        contentDescription = "No image",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            // Product Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.productName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                // Category Tag
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.category,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
                
                // Notes if available
                if (hasNotes) {
                    Text(
                        text = "Ghi chú: ${item.notes}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                
                Text(
                    text = formattedPrice,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            
            // Quantity Controls and Actions
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Quantity Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decrease Button (-)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (canDecrease) RedPrimary else Color(0xFFE0E0E0),
                                shape = CircleShape
                            )
                            .clickable(
                                enabled = canDecrease,
                                onClick = onQuantityDecrease
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canDecrease) Color.White else Color.Gray
                        )
                    }
                    
                    Text(
                        text = item.quantity.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.width(24.dp),
                        maxLines = 1
                    )
                    
                    // Increase Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                RedPrimary,
                                shape = CircleShape
                            )
                            .clickable(onClick = onQuantityIncrease),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tăng",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                // Remove Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            RedPrimary,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash),
                        contentDescription = "Xóa",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TableOrderDetailScreenPreview() {
    OneFoodTheme {
        TableOrderDetailScreen(
            navController = rememberNavController(),
            tableId = 4
        )
    }
}

