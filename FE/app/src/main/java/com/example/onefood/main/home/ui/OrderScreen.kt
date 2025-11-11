package com.example.onefood.main.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.onefood.R
import com.example.onefood.core.components.BottomTabBar
import com.example.onefood.data.model.Product
import com.example.onefood.main.home.viewmodel.ProductViewModel
import com.example.onefood.ui.theme.BoxProduct
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import com.example.onefood.ui.theme.colorCategory
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreenTopBar(navController: NavController, tableId: Int) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "ONE FOOD",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RedPrimary
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
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .background(Color(0xFFFFF0F0), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Bàn #$tableId",
                    color = RedPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            navigationIconContentColor = Color.Black,
            titleContentColor = RedPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    navController: NavController,
    tableId: Int,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val products by productViewModel.products.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val error by productViewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var isFilterExpanded by remember { mutableStateOf(false) }

    val categories = remember(products) {
        products
            .mapNotNull { it.category.takeIf { category -> category.isNotBlank() } }
            .distinct()
            .sorted()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        val normalizedQuery = searchQuery.trim().lowercase()
        products.filter { product ->
            product.isActive &&
                (selectedCategory.isNullOrBlank() || product.category.equals(selectedCategory, ignoreCase = true)) &&
                (normalizedQuery.isBlank() || listOf(
                    product.name.lowercase(),
                    product.category.lowercase(),
                    product.description.orEmpty().lowercase()
                ).any { it.contains(normalizedQuery) })
        }
    }

    Scaffold(
        topBar = { OrderScreenTopBar(navController, tableId) },
        bottomBar = { BottomTabBar(navController) },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = Color.Gray
                            )
                        },
                        placeholder = { Text(text = "Tìm kiếm", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thể loại",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        CategoryChip(
                            title = "Tất cả",
                            isSelected = selectedCategory.isNullOrBlank(),
                            onClick = { selectedCategory = null }
                        )
                    }
                    items(categories) { category ->
                        CategoryChip(
                            title = category,
                            isSelected = selectedCategory.equals(category, ignoreCase = true),
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gợi ý",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = RedPrimary)
                        }
                    }
                }

                error != null -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = error ?: "Đã xảy ra lỗi",
                                color = Color.Red,
                                fontSize = 16.sp
                            )
                            Button(onClick = { productViewModel.refreshProducts() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }

                filteredProducts.isEmpty() -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Không tìm thấy sản phẩm phù hợp", color = Color.Gray)
                        }
                    }
                }

                else -> {
                    val productPairs = filteredProducts.chunked(2)
                    items(productPairs) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            pair.forEach { product ->
                                OrderProductCard(
                                    product = product,
                                    modifier = Modifier.weight(1f),
                                    onProductClick = {
                                        val encodedName = URLEncoder.encode(product.name, "UTF-8")
                                        navController.navigate("product_detail/$tableId/${product.id}/$encodedName/${product.priceInt}")
                                    }
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(title: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) RedPrimary else Color(0xFFF5F5F5)
    val textColor = if (isSelected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun OrderProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onProductClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(270.dp)
            .clickable(onClick = onProductClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BoxProduct)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = product.getDisplayImageUrl()
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.img_productwhite),
                        error = painterResource(id = R.drawable.img_productwhite)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo_cart),
                        contentDescription = "No image",
                        tint = RedPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (product.category.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = product.category,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorCategory
                        )
                    }
                }

                Text(
                    text = formatCurrency(product.priceInt),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RedPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Xem chi tiết", fontSize = 14.sp, color = Color.Black)

                IconButton(onClick = onProductClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo_cart),
                        contentDescription = "Xem chi tiết",
                        tint = RedPrimary
                    )
                }
            }
        }
    }
}

private fun formatCurrency(value: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return formatter.format(value) + " ₫"
}

@Composable
fun OrderScreenPreview() {
    OneFoodTheme {
        OrderScreen(navController = rememberNavController(), tableId = 1)
    }
}
