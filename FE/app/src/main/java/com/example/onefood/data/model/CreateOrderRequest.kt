package com.example.onefood.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    @SerialName("table_id")
    val tableId: Int,
    val items: List<OrderItemRequest>
)

@Serializable
data class OrderItemRequest(
    @SerialName("product_id")
    val productId: Int,
    val quantity: Int,
    val price: Double,
    val notes: String = ""
)

