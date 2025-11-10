package com.example.onefood.data.model

data class CartItem(
    val productId: Int,
    val productName: String,
    val category: String,
    val price: Int,
    var quantity: Int,
    val imageUrl: String? = null,
    val notes: String = ""
) {
    val totalPrice: Int
        get() = price * quantity
}

