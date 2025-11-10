package com.example.onefood.data.model

data class Order(
    val id: String,
    val staffName: String,
    val tableNumber: Int,
    val totalPrice: Int,
    val status: String, // "Hoàn thành", "Đang xử lý", etc.
    val orderTime: String? = null,
    val paymentTime: String? = null,
    val cashier: String? = null,
    val items: List<OrderItem> = emptyList()
)

data class OrderItem(
    val productName: String,
    val quantity: Int,
    val price: Int,
    val imageUrl: String? = null
)

