package com.example.onefood.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteProductResponse(
    val success: Boolean?,
    val message: String?
)

