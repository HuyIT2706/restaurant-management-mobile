package com.example.onefood.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user_id: Int? = null,
    val role: String? = null
)