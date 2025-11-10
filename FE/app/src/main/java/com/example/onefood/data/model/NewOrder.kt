package com.example.onefood.data.model

// Đơn hàng mới chưa được tiếp nhận
data class NewOrder(
    val id: String,
    val tableNumber: Int,
    val staffName: String,
    val totalPrice: Int,
    val status: String = "Mới", // "Mới", "Đang xử lý", "Hoàn thành"
    val items: List<OrderItem> = emptyList(),
    val uniqueId: String = "" // Unique identifier để tránh duplicate keys - phải được set khi tạo object
) {
    // Helper để lấy unique key
    fun getKey(): String {
        return if (uniqueId.isNotEmpty()) uniqueId else "${id}_${tableNumber}_${hashCode()}"
    }
}

