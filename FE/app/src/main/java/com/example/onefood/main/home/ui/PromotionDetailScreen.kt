package com.example.onefood.main.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onefood.data.model.PromotionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionDetailScreen(navController: NavController, id: Int) {
    val promotion = PromotionItem(
        id = id,
        code = "FLASH10",
        startDate = "01/09/2025",
        endDate = "15/09/2025",
        status = true,
        discount = "10",
        quantity = 5,
        discountType = "PhầnTrăm",
        minOrderValue = "3.000.000",
        description = "Giảm 10% cho đơn hàng FLASH10"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Chi tiết khuyến mãi", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailField(label = "Mã khuyến mãi", value = promotion.code)
            DetailField(label = "Số lượng áp dụng", value = promotion.quantity.toString(), suffix = " lần")
            DetailField(
                label = "Loại giảm giá",
                value = if (promotion.discountType == "PhầnTrăm") "Phần trăm" else "Số tiền"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DetailField(label = "Ngày bắt đầu", value = promotion.startDate, modifier = Modifier.weight(1f))
                DetailField(label = "Ngày kết thúc", value = promotion.endDate, modifier = Modifier.weight(1f))
            }

            DetailField(
                label = "Trạng thái",
                value = if (promotion.status) "Hoạt động" else "Ngừng",
                valueColor = if (promotion.status) Color(0xFF4CAF50) else Color(0xFFF44336)
            )

            DetailField(
                label = "Giá trị giảm",
                value = promotion.discount,
                suffix = if (promotion.discountType == "PhầnTrăm") "%" else " đ"
            )

            DetailField(label = "Đơn hàng tối thiểu", value = promotion.minOrderValue, prefix = "Từ ")

            OutlinedTextField(
                value = promotion.description,
                onValueChange = {},
                label = { Text("Mô tả khuyến mãi") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = LocalTextStyle.current.color,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate("promotion_edit/${promotion.id}") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Chỉnh sửa khuyến mãi", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    suffix: String? = null,
    valueColor: Color = LocalContentColor.current
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        prefix = prefix?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = valueColor,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        textStyle = LocalTextStyle.current.copy(color = valueColor)
    )
}