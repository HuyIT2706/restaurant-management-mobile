package com.example.onefood.main.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.onefood.main.home.viewmodel.ProductViewModel
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary


@Composable
fun ProductDetailViewScreen(
    navController: NavController,
    productId: Int,
    productName: String,
    productPrice: Int,
    viewModel: ProductViewModel = hiltViewModel()
) {
    // Get product from ViewModel
    val products by viewModel.products.collectAsState()
    val product = remember(products, productId) {
        products.find { it.id == productId }
    }

    val displayName = product?.name ?: productName
    val displayPrice = product?.priceInt ?: productPrice
    val displayDescription = product?.description ?: ""
    val displayImageUrl = product?.getDisplayImageUrl()
    val displayCategory = product?.category ?: ""

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Product Image
                if (displayImageUrl != null && displayImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = displayImageUrl,
                        contentDescription = displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.img_productwhite),
                        error = painterResource(id = R.drawable.img_productwhite)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_productwhite),
                            contentDescription = "Product Image",
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                // Back Button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(top = 50.dp, start = 15.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Product Title
            Text(
                text = displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 15.dp)
            )

            // Product Price
            Text(
                text = "${displayPrice.toString().replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ".")} ₫",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RedPrimary
            )

            // Product Category
            if (displayCategory.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F0F0),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = displayCategory,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Product Description
            if (displayDescription.isNotEmpty()) {
                Text(
                    text = displayDescription,
                    fontSize = 18.sp,
                    color = Color.Black,
                    lineHeight = 24.sp
                )
            } else {
                Text(
                    text = "Không có mô tả",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Spacer for bottom bar
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailViewScreenPreview() {
    OneFoodTheme {
        ProductDetailViewScreen(
            navController = rememberNavController(),
            productId = 1,
            productName = "Lẩu 4 ngăn",
            productPrice = 430000
        )
    }
}
