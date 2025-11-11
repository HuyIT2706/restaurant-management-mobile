package com.example.onefood.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProductResponse(
    val success: Boolean?,
    val message: String?
)

