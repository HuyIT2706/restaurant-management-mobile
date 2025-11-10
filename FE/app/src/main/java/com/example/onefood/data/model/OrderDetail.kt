package com.example.onefood.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDetailItem(
    @SerialName("order_detail_quantity")
    val quantity: String? = null,
    
    @SerialName("order_detail_price")
    val price: String? = null,
    
    @SerialName("order_detail_notes")
    val notes: String? = null,
    
    @SerialName("product_name")
    val productName: String? = null,
    
    @SerialName("image_url")
    val imageUrl: String? = null
) {
    val quantityInt: Int
        get() = quantity?.toIntOrNull() ?: 0
    
    val priceInt: Int
        get() = price?.toDoubleOrNull()?.toInt() ?: 0
    
    val totalPrice: Int
        get() = quantityInt * priceInt
    
    fun getDisplayImageUrl(): String? {
        return imageUrl?.replace("http://localhost", "http://10.0.2.2")
            ?.replace("https://localhost", "http://10.0.2.2")
    }
}

@Serializable
data class OrderDetail(
    @SerialName("order_id")
    val orderId: String? = null,
    
    @SerialName("order_date")
    val orderDate: String? = null,
    
    @SerialName("order_status")
    val orderStatus: String? = null,
    
    @SerialName("order_totalamount")
    val orderTotalAmount: String? = null,
    
    @SerialName("order_updated_at")
    val orderUpdatedAt: String? = null,
    
    @SerialName("table_name")
    val tableName: String? = null,
    
    @SerialName("table_id")
    val tableId: String? = null,
    
    @SerialName("order_user_name")
    val orderUserName: String? = null,
    
    @SerialName("cash_user_name")
    val cashUserName: String? = null,
    
    @SerialName("items")
    val items: List<OrderDetailItem>? = null
) {
    val id: String
        get() = orderId ?: ""
    
    val status: String
        get() = orderStatus ?: ""
    
    val totalAmount: Int
        get() = orderTotalAmount?.toDoubleOrNull()?.toInt() ?: 0
    
    val tableNumber: String
        get() = tableName ?: tableId ?: ""
    
    val staffName: String
        get() = orderUserName ?: ""
    
    val cashier: String
        get() = cashUserName ?: ""
    
    val orderTime: String
        get() = orderDate ?: ""
    
    val paymentTime: String
        get() = orderUpdatedAt ?: ""
    
    val orderItems: List<OrderDetailItem>
        get() = items ?: emptyList()
}

@Serializable
data class OrderDetailResponse(
    val success: Boolean? = null,
    val data: OrderDetail? = null,
    val message: String? = null
)

