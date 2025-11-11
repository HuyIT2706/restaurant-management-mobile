package com.example.onefood.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddProductRequest(
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    @SerialName("image_url")
    val imageUrl: String
)

