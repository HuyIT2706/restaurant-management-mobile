package com.example.onefood.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderListItem(
    @SerialName("order_id")
    val orderId: String? = null,
    
    @SerialName("full_name")
    val fullName: String? = null,
    
    @SerialName("order_date_formatted")
    val orderDateFormatted: String? = null,
    
    @SerialName("payment_method")
    val paymentMethod: String? = null,
    
    @SerialName("table_name")
    val tableName: String? = null,
    
    @SerialName("table_id")
    val tableId: String? = null
) {
    // Helper properties
    val id: String
        get() = orderId ?: ""
    
    val staffName: String
        get() = fullName ?: ""
    
    val orderTime: String
        get() = orderDateFormatted ?: ""
    
    val tableLabel: String
        get() = when {
            !tableName.isNullOrBlank() -> tableName
            !tableId.isNullOrBlank() -> "Bàn $tableId"
            else -> ""
        }
    
    // Status: "Hoàn thành" nếu có payment_method và không phải "Chưa thanh toán"
    // "Tiếp nhận" nếu payment_method là "Chưa thanh toán" hoặc null
    val status: String
        get() = if (paymentMethod.isNullOrBlank() || paymentMethod == "Chưa thanh toán") {
            "Tiếp nhận"
        } else {
            "Hoàn thành"
        }
}

@Serializable
data class OrderListResponse(
    val success: Boolean? = null,
    val orders: List<OrderListItem>? = null
)

