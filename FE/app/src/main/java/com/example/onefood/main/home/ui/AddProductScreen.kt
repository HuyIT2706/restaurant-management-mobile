package com.example.onefood.main.home.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
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
import com.example.onefood.main.home.viewmodel.AddProductState
import com.example.onefood.main.home.viewmodel.AddProductViewModel
import com.example.onefood.main.home.viewmodel.ProductViewModel
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import java.io.ByteArrayOutputStream

@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: AddProductViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var productName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    
    // Get categories from products
    val products by productViewModel.products.collectAsState()
    val categories = remember(products) {
        listOf("") + products.mapNotNull { it.categoryName }
            .distinct()
            .sorted()
    }
    
    // Observe ViewModel state
    val addProductState by viewModel.state.collectAsState()
    
    var shouldClose by remember { mutableStateOf(false) }
    
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
    
    // Observe ProductViewModel loading state to know when refresh is complete
    val isProductLoading by productViewModel.isLoading.collectAsState()
    
    // Handle state changes and show toast
    LaunchedEffect(addProductState) {
        when (val state = addProductState) {
            is AddProductState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                // Refresh products list - this will update the shared ViewModel state
                productViewModel.refreshProducts()
                shouldClose = true
            }
            is AddProductState.Error -> {
                Toast.makeText(context, state.message ?: "Đã xảy ra lỗi", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }
    
    // Navigate back when refresh completes after successful product addition
    LaunchedEffect(addProductState, isProductLoading) {
        if (shouldClose && addProductState is AddProductState.Success && !isProductLoading) {
            // Wait a moment to ensure products state is updated in the UI
            kotlinx.coroutines.delay(200)
            // Navigate back only if we're still in success state
            if (addProductState is AddProductState.Success) {
                shouldClose = false
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
                                text = "THÊM MỚI SẢN PHẨM",
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
                                    )
                                    .clickable { imagePickerLauncher.launch(PickVisualMediaRequest()) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedImageUri != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(selectedImageUri),
                                        contentDescription = "Selected image",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_productwhite),
                                            contentDescription = "Chọn ảnh",
                                            modifier = Modifier.size(48.dp),
                                        )
                                        Text(
                                            text = "Chọn hình ảnh",
                                            fontSize = 14.sp,
                                            color = Color.Gray
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
                                value = productName,
                                onValueChange = { productName = it },
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
                                    value = selectedCategory ?: "",
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
                                    categories.filter { it.isNotEmpty() }.forEach { category ->
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
                        val isLoading = addProductState is AddProductState.Loading
                        val isValid = productName.isNotBlank() && 
                                     selectedCategory != null && 
                                     selectedCategory!!.isNotEmpty() && 
                                     price.isNotBlank() && 
                                     price.toDoubleOrNull() != null
                        
                        Button(
                            onClick = {
                                if (isValid && !isLoading) {
                                    val priceValue = price.toDoubleOrNull() ?: 0.0
                                    val fileName = selectedImageUri?.lastPathSegment ?: "product_image.jpg"
                                    viewModel.uploadImageAndAddProduct(
                                        context = context,
                                        imageBytes = imageBytes,
                                        fileName = fileName,
                                        name = productName.trim(),
                                        description = description.trim(),
                                        category = selectedCategory!!,
                                        price = priceValue
                                    )
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
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Thêm món",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Thêm món",
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
fun AddProductScreenPreview() {
    OneFoodTheme {
        AddProductScreen(navController = rememberNavController())
    }
}

