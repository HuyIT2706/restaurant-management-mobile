package com.example.onefood.main.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.onefood.R
import com.example.onefood.main.home.viewmodel.StatisticsViewModel
import com.example.onefood.ui.theme.RedPrimary
import com.example.onefood.core.components.BottomTabBar
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.text.NumberFormat
import java.util.*
import com.example.onefood.data.model.RevenueItem
import coil.compose.AsyncImage


@Composable
fun RevenueScreenTopBar() {
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
            contentDescription = "Logo ứng dụng",
            modifier = Modifier.size(40.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueListScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // Load revenue on first composition
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("onefood_prefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        if (token != null) {
            viewModel.loadRevenue(token)
        } else {
            Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
        }
    }

    val revenueJson by viewModel.revenueJson.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // Parse JSON to RevenueItems
    val items = remember(revenueJson) {
        val jsonString = revenueJson
        if (jsonString.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                val json = Json.parseToJsonElement(jsonString).jsonObject
                val itemsArray = json["items"]?.jsonArray ?: return@remember emptyList()
                itemsArray.mapNotNull { elem ->
                    try {
                        val obj = elem.jsonObject
                        RevenueItem(
                            id = obj["product_id"]?.jsonPrimitive?.content?.toIntOrNull(),
                            name = obj["product_name"]?.jsonPrimitive?.content,
                            category = obj["category"]?.jsonPrimitive?.content ?: "Sản phẩm",
                            quantity = obj["total_quantity"]?.jsonPrimitive?.content?.toIntOrNull(),
                            revenue = obj["total_revenue"]?.jsonPrimitive?.content
                                ?.replace(".", "")
                                ?.replace("đ", "")
                                ?.replace(" ", "")
                                ?.toIntOrNull(),
                            bestSeller = obj["best_seller"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false,
                            imageUrl = obj["image_url"]?.jsonPrimitive?.content
                        )

                    } catch (_: Exception) {
                        null
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    val filteredItems = items.filter {
        (it.name?.contains(searchQuery, ignoreCase = true) == true) ||
                (it.category?.contains(searchQuery, ignoreCase = true) == true)
    }

    Scaffold(
        topBar = { RevenueScreenTopBar() },
        bottomBar = { BottomTabBar(navController) },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm kiếm") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Tìm kiếm sản phẩm",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Loading
            if (loading && items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RedPrimary)
                }
            }
            // Empty
            else if (filteredItems.isEmpty() && !loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Không tìm thấy sản phẩm" else "Không có dữ liệu doanh thu",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            // List
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredItems) { item ->
                        RevenueListItem(
                            item = item,
                            onClick = {
                                item.id?.let { id ->
                                    navController.navigate("revenue_detail/$id")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueListItem(item: RevenueItem, onClick: () -> Unit) {
    val viLocale = Locale.Builder().setLanguage("vi").setRegion("VN").build()
    val formattedRevenue = NumberFormat.getInstance(viLocale).format(item.revenue ?: 0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Image
            AsyncImage(
                model = item.imageUrl ?: R.drawable.img_sp1,
                contentDescription = item.name ?: "Sản phẩm",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0)),
                contentScale = ContentScale.Crop
            )


            // Product Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.name ?: "Không có tên",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.bestSeller == true) {
                        Text(
                            text = "🔥 Bán chạy",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    text = item.category ?: "Sản phẩm",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "SL: ${item.quantity ?: 0}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            // Revenue
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$formattedRevenue đ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueDetailScreen(
    id: Int,
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Load chi tiết món
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("onefood_prefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        if (token != null) {
            viewModel.loadProductDetails(id, token)
        }
    }

    val detailJson by viewModel.productDetailsJson.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val viLocale = Locale.Builder().setLanguage("vi").setRegion("VN").build()

    // Parse dữ liệu chi tiết món
    val dish = remember(detailJson) {
        val jsonString = detailJson
        if (jsonString.isNullOrBlank()) {
            RevenueItem(id, "Đang tải...", "Sản phẩm", 0, 0, false, null, null)
        } else {
            try {
                val json = Json.parseToJsonElement(jsonString).jsonObject
                val productObj = json["product"]?.jsonObject
                val totalStatsObj = json["total_stats"]?.jsonObject
                
                RevenueItem(
                    id = productObj?.get("product_id")?.jsonPrimitive?.content?.toIntOrNull() ?: id,
                    name = productObj?.get("product_name")?.jsonPrimitive?.content,
                    category = productObj?.get("product_category")?.jsonPrimitive?.content ?: "Sản phẩm",
                    quantity = totalStatsObj?.get("total_quantity")?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                    revenue = totalStatsObj?.get("total_revenue")?.jsonPrimitive?.content
                        ?.replace(".", "")
                        ?.replace("đ", "")
                        ?.replace(" ", "")
                        ?.replace(",", "")
                        ?.toIntOrNull() ?: 0,
                    bestSeller = false,
                    imageUrl = productObj?.get("image_url")?.jsonPrimitive?.content
                )
            } catch (e: Exception) {
                RevenueItem(id, "Lỗi tải dữ liệu", "Sản phẩm", 0, 0, false, null, null)
            }
        }
    }

    // Parse danh sách đơn hàng từ daily_stats
    val orders = remember(detailJson) {
        try {
            val jsonString = detailJson
            if (jsonString.isNullOrBlank()) {
                emptyList()
            } else {
                val json = Json.parseToJsonElement(jsonString).jsonObject
                val dailyStatsArray = json["daily_stats"]?.jsonArray ?: return@remember emptyList()
                dailyStatsArray.mapNotNull { elem ->
                    val obj = elem.jsonObject
                    val orderDate = obj["order_date"]?.jsonPrimitive?.content ?: ""
                    val orderCount = obj["order_count"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    mapOf(
                        "order_id" to "Đơn: $orderCount",
                        "quantity" to (obj["daily_quantity"]?.jsonPrimitive?.content?.toIntOrNull()?.toString() ?: "0"),
                        "price" to (obj["daily_revenue"]?.jsonPrimitive?.content ?: "0 đ"),
                        "timestamp" to orderDate
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Parse tổng số lượng và tổng tiền
    val totalQuantity = remember(detailJson) {
        try {
            val jsonString = detailJson
            if (jsonString.isNullOrBlank()) {
                0
            } else {
                val json = Json.parseToJsonElement(jsonString).jsonObject
                val totalStatsObj = json["total_stats"]?.jsonObject
                totalStatsObj?.get("total_quantity")?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    val totalRevenue = remember(detailJson) {
        try {
            val jsonString = detailJson
            if (jsonString.isNullOrBlank()) {
                "0 đ"
            } else {
                val json = Json.parseToJsonElement(jsonString).jsonObject
                val totalStatsObj = json["total_stats"]?.jsonObject
                totalStatsObj?.get("total_revenue")?.jsonPrimitive?.content ?: "0 đ"
            }
        } catch (e: Exception) {
            "0 đ"
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Chi tiết doanh thu",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RedPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ảnh món
                AsyncImage(
                    model = dish.imageUrl ?: R.drawable.img_sp1,
                    contentDescription = dish.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )


                Spacer(modifier = Modifier.height(16.dp))

                // Tên món
                dish.name?.let {
                    Text(
                        text = it,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

// Bảng chi tiết đơn hàng
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        // ====== Header ======
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mã đơn",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.2f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "SL",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.6f),
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = "Giá",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                            Text(
                                text = "Thời gian",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.5f),
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }

                        HorizontalDivider(color = Color(0xFFE0E0E0))

                        // ====== Dữ liệu ======
                        if (orders.isEmpty()) {
                            Text(
                                text = "Không có dữ liệu đơn hàng",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color.Gray
                            )
                        } else {
                            orders.forEach { order ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = order["order_id"] ?: "",
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    Text(
                                        text = order["quantity"] ?: "",
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(0.6f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Text(
                                        text = order["price"] ?: "",
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                                    )

                                    // 🕒 Cột thời gian: Format từ YYYY-MM-DD sang DD/MM/YYYY
                                    val orderDate = order["timestamp"] ?: ""
                                    val formattedDate = try {
                                        if (orderDate.isNotEmpty() && orderDate.contains("-")) {
                                            val parts = orderDate.split("-")
                                            if (parts.size == 3) {
                                                "${parts[2]}/${parts[1]}/${parts[0]}"
                                            } else {
                                                orderDate
                                            }
                                        } else {
                                            orderDate
                                        }
                                    } catch (e: Exception) {
                                        orderDate
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .padding(vertical = 2.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = formattedDate,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFE0E0E0))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ====== Tổng kết ======
                        HorizontalDivider(color = Color(0xFFE0E0E0))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tổng SL:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.2f),
                                color = Color.Black
                            )
                            Text(
                                text = "$totalQuantity",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color.Black
                            )
                            Text(
                                text = "Tổng tiền:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                color = Color.Black
                            )
                            Text(
                                text = totalRevenue,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.5f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                color = RedPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}