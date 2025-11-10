package com.example.onefood.main.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.activity.ComponentActivity
import com.example.onefood.R
import com.example.onefood.core.components.BottomTabBar
import com.example.onefood.main.home.viewmodel.CartViewModel
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import com.example.onefood.util.formatPrice

// Helper function to get Activity-scoped CartViewModel
// With Hilt, viewModel() will automatically use HiltViewModelFactory if Activity has @AndroidEntryPoint
@Composable
fun rememberCartViewModel(): CartViewModel {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
        ?: throw IllegalStateException("CartViewModel requires Activity context")
    
    // Use viewModel() with Activity scope - Hilt will auto-inject dependencies
    return viewModel(
        modelClass = CartViewModel::class.java,
        viewModelStoreOwner = activity
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel = rememberCartViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }
    var selectedTableId by remember { mutableStateOf<Int?>(null) }
    
    // Debounce search query (300ms)
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedSearchQuery = searchQuery
    }
    
    // Get cart items from ViewModel - this will trigger recomposition when cart changes
    val cartItems by cartViewModel.cartItems.collectAsState()
    
    // Use derivedStateOf for tables with cart - only recomputes when cartItems changes
    val tablesWithCart by remember(cartItems) {
        derivedStateOf {
            cartItems.keys.filter { tableId ->
                val items = cartItems[tableId] ?: emptyList()
                items.isNotEmpty()
            }.sorted()
        }
    }
    
    // Use derivedStateOf for filtered tables - only recomputes when dependencies change
    val filteredTables by remember(tablesWithCart, debouncedSearchQuery, cartItems) {
        derivedStateOf {
            if (debouncedSearchQuery.isBlank()) {
                tablesWithCart
            } else {
                tablesWithCart.filter { tableId ->
                    val items = cartItems[tableId] ?: emptyList()
                    val totalPrice = items.sumOf { it.totalPrice }
                    tableId.toString().contains(debouncedSearchQuery, ignoreCase = true) ||
                    totalPrice.toString().contains(debouncedSearchQuery, ignoreCase = true)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Giỏ hàng", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(
                        onClick = { 
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                navController.navigate("home/QuanLy")
                            }
                        }
                    ) {
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
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
            
            if (filteredTables.isEmpty()) {
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
                            contentDescription = "Không có giỏ hàng",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "Không có bàn nào có giỏ hàng",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Tables with cart List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredTables,
                        key = { "table_$it" } // Optimized key generation
                    ) { tableId ->
                        // Cache calculations for this table
                        val tableData = remember(tableId, cartItems) {
                            val items = cartItems[tableId] ?: emptyList()
                            Pair(items.sumOf { it.totalPrice }, items.size)
                        }
                        val totalPrice = tableData.first
                        val itemCount = tableData.second
                        
                        TableCartCard(
                            tableId = tableId,
                            itemCount = itemCount,
                            totalPrice = totalPrice,
                            isSelected = selectedTableId == tableId,
                            onClick = {
                                selectedTableId = tableId
                                // Navigate to order detail screen
                                navController.navigate("table_order_detail/$tableId")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TableCartCard(
    tableId: Int,
    itemCount: Int,
    totalPrice: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Cache formatted price
    val formattedPrice = remember(totalPrice) { totalPrice.formatPrice() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, Color(0xFF2196F3), RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            )
    ) {
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
                // Header với Table Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bàn $tableId",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    // Item count badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RedPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "$itemCount món",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Total Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tổng tiền:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = formattedPrice,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedPrimary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    OneFoodTheme {
        CartScreen(navController = rememberNavController())
    }
}
