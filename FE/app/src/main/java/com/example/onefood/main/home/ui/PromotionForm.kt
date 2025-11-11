package com.example.onefood.main.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun PromotionForm(
    code: MutableState<TextFieldValue>,
    startDate: MutableState<TextFieldValue>,
    endDate: MutableState<TextFieldValue>,
    discount: MutableState<TextFieldValue>,
    quantity: MutableState<TextFieldValue>,
    onSubmit: () -> Unit
) {
    Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = code.value, onValueChange = { code.value = it }, label = { Text("Mã khuyến mãi") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = startDate.value, onValueChange = { startDate.value = it }, label = { Text("Ngày bắt đầu") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = endDate.value, onValueChange = { endDate.value = it }, label = { Text("Ngày kết thúc") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = discount.value, onValueChange = { discount.value = it }, label = { Text("Giảm giá (%) hoặc số tiền") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = quantity.value, onValueChange = { quantity.value = it }, label = { Text("Số lượng") }, modifier = Modifier.fillMaxWidth())

        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
            Text("Lưu khuyến mãi")
        }
    }
}