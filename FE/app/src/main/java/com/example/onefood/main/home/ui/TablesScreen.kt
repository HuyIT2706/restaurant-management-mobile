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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.onefood.R
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.onefood.data.model.Table
import com.example.onefood.main.home.viewmodel.SelectTableViewModel
import com.example.onefood.main.home.viewmodel.SelectTableState
import com.example.onefood.main.home.viewmodel.TablesViewModel
import com.example.onefood.ui.theme.OneFoodTheme
import com.example.onefood.ui.theme.RedPrimary
import com.example.onefood.ui.theme.TableBeige
import com.example.onefood.ui.theme.TableGreen
import com.example.onefood.core.components.BottomTabBar
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect


@Composable
fun TableScreenTopBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 15.dp, end = 15.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "ONE FOOD",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RedPrimary
        )

        Image(
            painter = painterResource(id = R.drawable.ic_logo_cart),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(40.dp)
        )
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
    val context = LocalContext.current
    val viewModel: TablesViewModel = hiltViewModel()
    val selectTableViewModel: SelectTableViewModel = hiltViewModel()
    
    val tables by viewModel.tables.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val selectTableState by selectTableViewModel.state.collectAsState()
    
    var selectedTable by remember { mutableStateOf<Table?>(null) }
    var pendingTableId by remember { mutableStateOf<Int?>(null) }
    
    // Handle select table state changes
    LaunchedEffect(selectTableState) {
        when (val state = selectTableState) {
            is SelectTableState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                // Refresh tables list để cập nhật trạng thái mới
                viewModel.loadTables()
                // Điều hướng sang màn gọi món nếu có bàn đang chờ
                pendingTableId?.let { tableId ->
                    kotlinx.coroutines.delay(200)
                    navController.navigate("order_menu_route/$tableId") {
                        launchSingleTop = true
                    }
                }
                // Reset state sau khi điều hướng
                pendingTableId = null
                selectedTable = null
                selectTableViewModel.resetState()
            }
            is SelectTableState.Error -> {
                Toast.makeText(context, state.message ?: "Không thể chọn bàn", Toast.LENGTH_LONG).show()
                selectTableViewModel.resetState()
            }
            else -> {}
        }
    }

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
                        val statusNormalized = normalizeStatus(table.status ?: "")
                        val isAvailable = statusNormalized in setOf("trong", "empty", "available", "free")
                        
                        TableCard(
                            table = table,
                            isEnabled = isAvailable,
                            onClick = {
                                if (isAvailable) {
                                    selectedTable = table
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    selectedTable?.let { table ->
        AlertDialog(
            onDismissRequest = { selectedTable = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(1.dp))
                    IconButton(
                        onClick = { selectedTable = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Text(
                    text = "Bạn muốn chọn bàn : ${table.name} ?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            confirmButton = {
                val isSelecting = selectTableState is SelectTableState.Loading
                Button(
                    onClick = {
                        table.id?.let { tableId ->
                            pendingTableId = tableId
                            selectTableViewModel.selectTable(tableId)
                        }
                    },
                    enabled = !isSelecting,
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                    modifier = Modifier.padding(bottom = 16.dp, end = 16.dp).offset(x = (20.dp))
                ) {
                    if (isSelecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                    Text("Xác nhận", color = Color.White, fontSize = 16.sp)
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun normalizeStatus(input: String): String {
    val normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    return normalized.lowercase().trim()
}

@Composable
fun TableCard(table: Table, isEnabled: Boolean, onClick: () -> Unit) {
    val statusNormalized = normalizeStatus(table.status ?: "")
    val isAvailable = statusNormalized in setOf("trong", "empty", "available", "free")

    val cardColor = if (isAvailable) TableGreen else TableBeige
    val textColor = if (isAvailable) Color.White else Color.Black
    val opacity = if (isEnabled) 1f else 0.5f

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .height(90.dp)
            .alpha(opacity)
            .then(
                if (isEnabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
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