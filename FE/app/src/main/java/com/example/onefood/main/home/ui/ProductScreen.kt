package com.example.onefood.main.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.onefood.R
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.onefood.data.model.Product
import com.example.onefood.main.home.viewmodel.DeleteProductViewModel
import com.example.onefood.main.home.viewmodel.DeleteProductState
import com.example.onefood.main.home.viewmodel.ProductViewModel
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import com.example.onefood.ui.theme.colorCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    navController: NavController,
    viewModel: ProductViewModel = hiltViewModel(),
    deleteViewModel: DeleteProductViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    
    // Observe ViewModel state
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Observe DeleteProductViewModel state
    val deleteState by deleteViewModel.state.collectAsState()
    
    // Track previous route to detect when returning from Add/Update screens
    var previousRoute by remember { mutableStateOf<String?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Refresh products when returning to this screen from Add/Update screens
    LaunchedEffect(currentRoute) {
        if (currentRoute == "product_route") {
            // Only refresh if we're returning from Add/Update screen (not initial load)
            if (previousRoute != null && previousRoute != "product_route") {
                // Small delay to ensure navigation is complete
                kotlinx.coroutines.delay(100)
                viewModel.refreshProducts()
            }
            previousRoute = currentRoute
        } else {
            previousRoute = currentRoute
        }
    }
    
    // Handle delete state changes
    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is DeleteProductState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                deleteViewModel.resetState()
                // Refresh products list
                viewModel.refreshProducts()
                // Close dialog
                showDeleteDialog = false
                productToDelete = null
            }
            is DeleteProductState.Error -> {
                Toast.makeText(context, state.message ?: "Đã xảy ra lỗi", Toast.LENGTH_LONG).show()
                deleteViewModel.resetState()
                // Keep dialog open so user can retry
            }
            else -> {}
        }
    }
    
    // Get all unique categories from products - cached for performance
    val categories = remember(products) {
        products.mapNotNull { it.categoryName }
            .distinct()
            .sorted()
    }
    
    // Filter products based on search query and category - optimized with remember
    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        if (searchQuery.isEmpty() && selectedCategory == null) {
            // No filter applied, return only active products
            products.filter { it.isActive }
        } else {
            products.filter { product ->
                // Filter by active status
                if (!product.isActive) return@filter false
                
                // Filter by category
                if (selectedCategory != null && product.categoryName != selectedCategory) {
                    return@filter false
                }
                
                // Filter by search query
                if (searchQuery.isNotEmpty()) {
                    val query = searchQuery.lowercase()
                    val matchesSearch = product.name.lowercase().contains(query) ||
                        product.category.lowercase().contains(query) ||
                        product.description?.lowercase()?.contains(query) == true
                    if (!matchesSearch) return@filter false
                }
                
                true
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sản phẩm", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(44.dp)
                            .background(Color(0xFFFF4444), shape = CircleShape)
                            .clickable { navController.navigate("add_product_route") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Thêm",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Search bar and Category filter - Always visible at top
            var expanded by remember { mutableStateOf(false) }
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
                        placeholder = { Text("Tìm kiếm sản phẩm...") },
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
                

                    Box(modifier = Modifier.wrapContentSize()) {
                        IconButton(
                            onClick = { expanded = true },
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    color = if (selectedCategory != null) RedPrimary else Color(0xFFF5F5F5),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_filter),
                                contentDescription = "Lọc theo loại",
                                tint = if (selectedCategory != null) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.widthIn(max = 200.dp)
                        ) {
                            // Option: All categories
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Tất cả",
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedCategory == null) FontWeight.Medium else FontWeight.Normal
                                    ) 
                                },
                                onClick = {
                                    selectedCategory = null
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = if (selectedCategory == null) RedPrimary else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            
                            // Category options
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            category,
                                            fontSize = 14.sp,
                                            fontWeight = if (selectedCategory == category) FontWeight.Medium else FontWeight.Normal
                                        ) 
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (selectedCategory == category) RedPrimary else Color.Transparent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Show selected category badge
                if (selectedCategory != null) {
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
                                    selectedCategory = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCategory!!,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = RedPrimary
                                )
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Xóa lọc",
                                    modifier = Modifier.size(14.dp),
                                    tint = RedPrimary
                                )
                            }
                        }
                    }
                }
            }
            
            // Content area - Products list with loading/error states
            Box(modifier = Modifier.fillMaxSize()) {
                // Loading State
                if (isLoading && products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = RedPrimary)
                    }
                }
                // Error State
                else if (error != null && products.isEmpty()) {
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
                            Text(
                                text = "Lỗi: $error",
                                fontSize = 16.sp,
                                color = Color.Red
                            )
                            Button(
                                onClick = { viewModel.refreshProducts() },
                                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                // Product List
                else {
                    if (filteredProducts.isEmpty() && !isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty() || selectedCategory != null) "Không tìm thấy sản phẩm" else "Không có sản phẩm nào",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = filteredProducts,
                                key = { product -> product.id ?: product.productId ?: 0 }
                            ) { product ->
                                ProductItemCard(
                                    product = product,
                                    onEditClick = {
                                        val encodedName = java.net.URLEncoder.encode(product.name, "UTF-8")
                                        val encodedCategory = java.net.URLEncoder.encode(product.category, "UTF-8")
                                        val encodedDescription = java.net.URLEncoder.encode(product.description ?: "", "UTF-8")
                                        navController.navigate("update_product_route/${product.id}/$encodedName/$encodedCategory/${product.priceInt}/$encodedDescription")
                                    },
                                    onDeleteClick = {
                                        productToDelete = product
                                        showDeleteDialog = true
                                    },
                                    onProductClick = {
                                        val encodedName = java.net.URLEncoder.encode(product.name, "UTF-8")
                                        navController.navigate("product_view_detail/${product.id}/$encodedName/${product.priceInt}")
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Show loading indicator overlay when loading with existing products
                if (isLoading && products.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = RedPrimary)
                    }
                }
            }
            
            // Delete Confirmation Dialog
            if (showDeleteDialog && productToDelete != null) {
                DeleteProductDialog(
                    productName = productToDelete!!.name,
                    isLoading = deleteState is DeleteProductState.Loading,
                    onConfirm = {
                        productToDelete?.let { product ->
                            deleteViewModel.deleteProduct(context, product.id)
                        }
                    },
                    onDismiss = {
                        if (deleteState !is DeleteProductState.Loading) {
                            showDeleteDialog = false
                            productToDelete = null
                            deleteViewModel.resetState()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onProductClick: () -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable(onClick = onProductClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val displayImageUrl = product.getDisplayImageUrl()
                if (displayImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(displayImageUrl)
                            .size(Size(160, 160)) // Resize to 2x for retina displays (80dp * 2)
                            .memoryCacheKey(displayImageUrl)
                            .diskCacheKey(displayImageUrl)
                            .build(),
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        placeholder = painterResource(id = R.drawable.avatarsignin),
                        error = painterResource(id = R.drawable.avatarsignin)
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
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                

                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFFCFCFC),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = product.category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorCategory
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Price
                Text(
                    text = "${product.priceInt.toString().replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ".")} ₫",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedPrimary
                )
            }
            
            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_update),
                        contentDescription = "Chỉnh sửa",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Delete Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            RedPrimary,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(onClick = onDeleteClick),
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

@Composable
fun DeleteProductDialog(
    productName: String,
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "XOÁ SẢN PHẨM",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(
                        onClick = onDismiss,
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
                
                // Message
                Text(
                    text = "Bạn có chắc chắn muốn xoá sản phẩm này ?",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // OK Button
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "OK",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    // Cancel Button
                    Button(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp)
                    ) {
                        Text(
                            text = "HUỶ",
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

@Preview(showBackground = true)
@Composable
fun ProductScreenPreview() {
    OneFoodTheme {
        ProductScreen(navController = rememberNavController())
    }
}

