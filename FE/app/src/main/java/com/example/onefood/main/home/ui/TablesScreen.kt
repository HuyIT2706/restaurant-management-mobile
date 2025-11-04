package com.example.onefood.main.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.onefood.R
import com.example.onefood.data.model.Table
import com.example.onefood.main.home.viewmodel.TablesViewModel
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import com.example.onefood.ui.theme.TableBeige
import com.example.onefood.ui.theme.TableGreen
import com.example.onefood.core.components.BottomTabBar
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun TableScreenTopBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 15.dp, end = 15.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo_cart),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(40.dp)
        )

        Text(
            text = "ONE FOOD",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RedPrimary
        )

        IconButton(onClick = { /* Mở Sidebar Menu */ }) {
            Icon(painterResource(id = R.drawable.ic_menu_hamburguer), contentDescription = "Menu", tint = Color.Black, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun TableSearchBarComponent() {
    OutlinedTextField(
        value = "",
        onValueChange = { /* Xử lý tìm kiếm */ },
        placeholder = { Text("Tìm kiếm") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            unfocusedContainerColor = Color(0xFFF0F0F0),
            focusedContainerColor = Color(0xFFF0F0F0),
            cursorColor = RedPrimary
        ),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    )
}

@Composable
fun TablesScreen(navController: NavController) {

    val viewModel: TablesViewModel = hiltViewModel()
    val tables by viewModel.tables.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = { TableScreenTopBar(navController) },
    bottomBar = { BottomTabBar(navController) },
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TableSearchBarComponent()

            Spacer(modifier = Modifier.height(24.dp))

            // Lưới hiển thị trạng thái bàn
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLoading) {
                    item(span = { GridItemSpan(3) }) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = RedPrimary)
                        }
                    }
                } else if (error != null) {
                    item(span = { GridItemSpan(3) }) {
                        Text(
                            text = error ?: "Unknown error occurred",
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(tables) { table ->
                        TableCard(table = table) {
                            navController.navigate("order_menu_route/${table.id ?: 0}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableCard(table: Table, onClick: () -> Unit) {

    fun normalize(input: String): String {
        val normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return normalized.lowercase().trim()
    }

    val statusNormalized = normalize(table.status ?: "")
    val isAvailable = statusNormalized in setOf("trong", "empty", "available", "free")

    val cardColor = if (isAvailable) TableGreen else TableBeige
    val textColor = if (isAvailable) Color.White else Color.Black

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .height(90.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = table.name ?: "",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TablesScreenPreview() {
    OneFoodTheme {
        TablesScreen(navController = rememberNavController())
    }
}