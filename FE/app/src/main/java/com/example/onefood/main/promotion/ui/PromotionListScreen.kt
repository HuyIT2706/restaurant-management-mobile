package com.example.onefood.main.promotion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionListScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tất cả") }
    var showDeleteDialog by remember { mutableStateOf<PromotionItem?>(null) }

    val promotions = remember {
        mutableStateListOf(
            // DỮ LIỆU CŨ (Phần trăm)
            PromotionItem(1, "FLASH10", "01/09/2025", "30/09/2025", true, "10%", 5),
            PromotionItem(2, "HOTDEAL50", "01/09/2025", "17/09/2025", true, "50%", 2),
            PromotionItem(3, "SALE20", "18/09/2025", "20/09/2025", true, "20%", 10),
            PromotionItem(4, "WELCOME10", "25/09/2025", "30/09/2025", false, "10%", 0),

            // DỮ LIỆU MỚI (Số tiền) – ĐÃ THÊM
            PromotionItem(5, "SHIP50K", "10/09/2025", "25/09/2025", true, "50.000 đ", 8),
            PromotionItem(6, "FREESHIP", "05/09/2025", "15/09/2025", true, "30.000 đ", 3),
            PromotionItem(7, "VOUCHER10K", "01/09/2025", "30/09/2025", true, "10.000 đ", 15)
        )
    }

    val filteredPromotions = promotions.filter {
        (selectedFilter == "Tất cả"
                || (selectedFilter == "Phần trăm" && it.discount.contains("%"))
                || (selectedFilter == "Số tiền" && it.discount.contains("đ"))) &&
                it.code.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khuyến mãi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("promotion_add") }) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm khuyến mãi")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            SearchBarWithFilter(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                onReset = {
                    searchQuery = ""
                    selectedFilter = "Tất cả"
                }
            )

            LazyColumn(Modifier.padding(horizontal = 12.dp)) {
                items(filteredPromotions) { promo ->
                    PromotionListItem(
                        promo = promo,
                        onClick = { navController.navigate("promotion_detail/${promo.id}") },
                        onDelete = { showDeleteDialog = promo }
                    )
                }
            }
        }

        showDeleteDialog?.let { selected ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Xóa khuyến mãi") },
                text = { Text("Bạn có chắc muốn xóa ${selected.code}?") },
                confirmButton = {
                    TextButton(onClick = {
                        promotions.remove(selected)
                        showDeleteDialog = null
                    }) { Text("OK", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) { Text("Hủy") }
                }
            )
        }
    }
}