package com.example.onefood.main.home.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.onefood.R
import com.example.onefood.data.model.Product
import com.example.onefood.main.home.viewmodel.ProductViewModel
import com.example.onefood.main.home.viewmodel.UpdateProductState
import com.example.onefood.main.home.viewmodel.UpdateProductViewModel
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import java.io.ByteArrayOutputStream

@Composable
fun UpdateProductScreen(
    navController: NavController,
    productId: Int,
    productName: String,
    productCategory: String,
    productPrice: Int,
    productDescription: String? = null,
    viewModel: UpdateProductViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(productName) }
    var selectedCategory by remember { mutableStateOf(productCategory) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var price by remember { mutableStateOf(productPrice.toString()) }
    var description by remember { mutableStateOf(productDescription ?: "") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    
    // Observe products from ViewModel (shared instance)
    val products by productViewModel.products.collectAsState()
    val categories = remember(products) {
        products.mapNotNull { it.categoryName }
            .distinct()
            .sorted()
    }
    
    // Get current product to get image URL
    val currentProduct = remember(products, productId) {
        products.find { it.id == productId }
    }
    val currentImageUrl = currentProduct?.getDisplayImageUrl()
    
    // Observe ViewModel state
    val updateState by viewModel.state.collectAsState()
    
    // Observe ProductViewModel loading state
    val isProductLoading by productViewModel.isLoading.collectAsState()
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            // Convert URI to ByteArray
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val outputStream = ByteArrayOutputStream()
                inputStream.copyTo(outputStream)
                imageBytes = outputStream.toByteArray()
            }
        }
    }
    
    // Track if we should navigate back (to prevent multiple navigations)
    var shouldNavigateBack by remember { mutableStateOf(false) }
    
    // Handle state changes and show toast
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateProductState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                // Refresh products list - this updates the shared ProductViewModel
                productViewModel.refreshProducts()
                // Set flag to navigate back after refresh completes
                shouldNavigateBack = true
            }
            is UpdateProductState.Error -> {
                Toast.makeText(context, state.message ?: "Đã xảy ra lỗi", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }
    
    // Navigate back when refresh completes and products are updated
    LaunchedEffect(shouldNavigateBack, isProductLoading) {
        if (shouldNavigateBack && !isProductLoading) {
            // Wait a moment to ensure products state is updated in the shared ViewModel
            // This is similar to AddProductScreen logic
            kotlinx.coroutines.delay(500)
            // Navigate back only if we're still in the state to navigate
            if (shouldNavigateBack) {
                shouldNavigateBack = false
                viewModel.resetState()
                navController.popBackStack()
            }
        }
    }

    Dialog(
        onDismissRequest = { navController.popBackStack() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            // Clickable overlay để đóng dialog khi click bên ngoài
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { navController.popBackStack() }
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.85f)
                    .clickable { }, // Prevent clicks from passing through
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
                    // Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CẬP NHẬT SẢN PHẨM",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Đóng",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                
                    // Image Upload Section
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(
                                        Color(0xFFF5F5F5),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    selectedImageUri != null -> {
                                        Image(
                                            painter = rememberAsyncImagePainter(model = selectedImageUri),
                                            contentDescription = "Selected image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    currentImageUrl != null && currentImageUrl.isNotEmpty() -> {
                                        Image(
                                            painter = rememberAsyncImagePainter(model = currentImageUrl),
                                            contentDescription = "Product image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_productwhite),
                                            contentDescription = "No image",
                                            modifier = Modifier.size(80.dp)
                                        )
                                    }
                                }
                            }
                            
                            Button(
                                onClick = { imagePickerLauncher.launch(PickVisualMediaRequest()) },
                                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (selectedImageUri != null) "Chọn hình ảnh khác" else "Chọn hình ảnh",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                
                    // Product Name Field
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Tên món",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("Nhập tên món", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE0E0E0),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                singleLine = true
                            )
                        }
                    }
                    
                    // Category Dropdown
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Chọn món",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Box {
                                OutlinedTextField(
                                    value = selectedCategory,
                                    onValueChange = { },
                                    readOnly = true,
                                    placeholder = { Text("Chọn danh mục", color = Color.Gray) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            tint = Color.Black,
                                            modifier = Modifier.clickable { showCategoryDropdown = true }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showCategoryDropdown = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFE0E0E0),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                
                                DropdownMenu(
                                    expanded = showCategoryDropdown,
                                    onDismissRequest = { showCategoryDropdown = false }
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category) },
                                            onClick = {
                                                selectedCategory = category
                                                showCategoryDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Price Field
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Giá bán",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            OutlinedTextField(
                                value = price,
                                onValueChange = { newValue ->
                                    // Only allow numbers
                                    if (newValue.all { it.isDigit() }) {
                                        price = newValue
                                    }
                                },
                                placeholder = { Text("Nhập giá bán", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE0E0E0),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                singleLine = true
                            )
                        }
                    }
                    
                    // Description Field
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Mô tả",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = { Text("Nhập mô tả món ăn...", color = Color.Gray) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE0E0E0),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                maxLines = 4
                            )
                        }
                    }
                    
                    // Submit Button
                    item {
                        val isLoading = updateState is UpdateProductState.Loading
                        val isValid = name.isNotBlank() && 
                                     selectedCategory.isNotEmpty() && 
                                     price.isNotBlank() && 
                                     price.toDoubleOrNull() != null
                        
                        Button(
                            onClick = {
                                if (isValid && !isLoading) {
                                    val priceValue = price.toDoubleOrNull() ?: 0.0
                                    // Store selectedImageUri and imageBytes in local variables to avoid smart cast issues
                                    val imageUri = selectedImageUri
                                    val bytes = imageBytes
                                    val fileName = if (imageUri != null) {
                                        imageUri.lastPathSegment ?: "product_image.jpg"
                                    } else {
                                        "product_image.jpg"
                                    }
                                    
                                    // Upload image if new image is selected, otherwise use current image URL
                                    if (bytes != null && bytes.isNotEmpty()) {
                                        // Will upload image first, then update product
                                        viewModel.updateProductWithImage(
                                            context = context,
                                            productId = productId,
                                            imageBytes = bytes,
                                            fileName = fileName,
                                            name = name.trim(),
                                            description = description.trim(),
                                            category = selectedCategory,
                                            price = priceValue,
                                            currentImageUrl = currentImageUrl ?: ""
                                        )
                                    } else {
                                        // Update product without changing image
                                        viewModel.updateProduct(
                                            context = context,
                                            productId = productId,
                                            name = name.trim(),
                                            description = description.trim(),
                                            category = selectedCategory,
                                            price = priceValue,
                                            imageUrl = currentImageUrl ?: "",
                                            productActive = 1
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        context, 
                                        "Vui lòng nhập đầy đủ thông tin!", 
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = isValid && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Sửa món",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sửa món",
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

@Preview(showBackground = true)
@Composable
fun UpdateProductScreenPreview() {
    OneFoodTheme {
        UpdateProductScreen(
            navController = rememberNavController(),
            productId = 1,
            productName = "Lẩu 4 ngăn",
            productCategory = "Lẩu",
            productPrice = 430000,
            productDescription = "Nồi lẩu với 4 ngăn"
        )
    }
}
