package com.example.onefood.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @SerialName("product_id")
    val productId: String? = null,
    
    @SerialName("product_name")
    val productName: String? = null,
    
    @SerialName("product_description")
    val productDescription: String? = null,
    
    @SerialName("price")
    val price: String? = null, // API trả về string, sẽ convert sang Int/Double
    
    @SerialName("image_url")
    val imageUrl: String? = null,
    
    @SerialName("product_active")
    val productActive: String? = null, // "1" hoặc "0", sẽ convert sang Boolean
    
    @SerialName("category_name")
    val categoryName: String? = null
) {
    // Helper properties để convert sang types phù hợp
    val id: Int
        get() = productId?.toIntOrNull() ?: 0
    
    val name: String
        get() = productName ?: ""
    
    val category: String
        get() = categoryName ?: ""
    
    val priceInt: Int
        get() = price?.toDoubleOrNull()?.toInt() ?: 0
    
    val isActive: Boolean
        get() = productActive == "1"
    
    val description: String?
        get() = productDescription
    
    // Helper để convert image URL từ localhost sang emulator address
    fun getDisplayImageUrl(): String? {
        return imageUrl?.replace("http://localhost", "http://10.0.2.2")
            ?.replace("https://localhost", "http://10.0.2.2")
    }
}

