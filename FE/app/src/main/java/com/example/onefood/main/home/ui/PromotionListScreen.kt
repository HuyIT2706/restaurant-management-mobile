package com.example.onefood.main.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.onefood.R
import com.example.onefood.data.model.PromotionItem
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionListScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var promotionToDelete by remember { mutableStateOf<PromotionItem?>(null) }
    
    // Sample data - sau này sẽ lấy từ ViewModel/API
    val promotions = remember {
        mutableStateListOf(
            PromotionItem(1, "FLASH10", "01/09/2025", "15/09/2025", true, "10%", 5, "PhầnTrăm", "3.000.000 đ", "Giảm 10% cho đơn hàng FLASH10"),
            PromotionItem(2, "SALE50K", "10/09/2025", "20/09/2025", true, "50.000 đ", 10, "SốTiền", "1.000.000 đ", "Giảm 50.000 đ cho đơn hàng trên 1 triệu"),
            PromotionItem(3, "WEEKEND20", "15/09/2025", "30/09/2025", false, "20%", 3, "PhầnTrăm", "2.000.000 đ", "Giảm 20% cuối tuần")
        )
    }
    
    val filteredPromotions = promotions.filter { 
        it.code.contains(searchQuery, ignoreCase = true) ||
        it.discount.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Khuyến mại", fontWeight = FontWeight.Medium) },
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
                            .clickable { navController.navigate("promotion_add") },
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
            
            // Promotion List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPromotions) { promotion ->
                    PromotionListItem(
                        promo = promotion,
                        onEditClick = {
                            // Navigate to edit screen with only ID (simpler and safer)
                            navController.navigate("promotion_edit/${promotion.id}")
                        },
                        onDelete = {
                            promotionToDelete = promotion
                            showDeleteDialog = true
                        },
                        onPromotionClick = {
                            // Navigate to detail screen
                            navController.navigate("promotion_detail/${promotion.id}")
                        }
                    )
                }
            }
            
            // Delete Confirmation Dialog
            if (showDeleteDialog && promotionToDelete != null) {
                DeletePromotionDialog(
                    promotionCode = promotionToDelete!!.code,
                    onConfirm = {
                        val promotionId = promotionToDelete!!.id
                        promotions.removeAll { it.id == promotionId }
                        showDeleteDialog = false
                        promotionToDelete = null
                    },
                    onDismiss = {
                        showDeleteDialog = false
                        promotionToDelete = null
                    }
                )
            }
        }
    }
}

@Composable
fun DeletePromotionDialog(
    promotionCode: String,
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
                        text = "XOÁ KHUYẾN MÃI",
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
                    text = "Bạn có chắc chắn muốn xoá khuyến mãi $promotionCode ?",
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
                        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp)
                    ) {
                        Text(
                            text = "OK",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // Cancel Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
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
fun PromotionListScreenPreview() {
    OneFoodTheme {
        PromotionListScreen(navController = rememberNavController())
    }
}
