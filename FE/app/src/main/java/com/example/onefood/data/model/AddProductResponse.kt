package com.example.onefood.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddProductResponse(
    val success: Boolean?,
    val message: String?,
    @SerialName("product_id")
    val productId: Int? = null
)

