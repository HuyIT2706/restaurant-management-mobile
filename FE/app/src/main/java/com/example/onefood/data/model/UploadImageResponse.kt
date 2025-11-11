package com.example.onefood.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadImageResponse(
    val success: Boolean,
    val message: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("file_name")
    val fileName: String? = null
)

