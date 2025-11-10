package com.example.onefood.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderResponse(
    val success: Boolean?,
    val message: String?,
    @SerialName("order_id")
    val orderId: Int? = null,
    @SerialName("total_amount")
    val totalAmount: Double? = null
)

