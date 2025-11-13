package com.example.onefood.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("user_id")
    val userId: Int? = null,
    @SerialName("fullname")
    val fullname: String? = null,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("wage")
    val wage: Double? = null,
    @SerialName("status")
    val status: Int? = null,
    @SerialName("image")
    val image: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class GetUsersResponse(
    @SerialName("status")
    val status: String? = null,
    @SerialName("total")
    val total: Int? = null,
    @SerialName("data")
    val data: List<User>? = null,
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class AddUserResponse(
    @SerialName("status")
    val status: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("user_id")
    val userId: Int? = null,
    @SerialName("user_firstname")
    val userFirstname: String? = null,
    @SerialName("user_lastname")
    val userLastname: String? = null
)

@Serializable
data class UpdateUserResponse(
    @SerialName("status")
    val status: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("user_id")
    val userId: Int? = null,
    @SerialName("user_firstname")
    val userFirstname: String? = null,
    @SerialName("user_lastname")
    val userLastname: String? = null
)

@Serializable
data class DeleteUserResponse(
    @SerialName("status")
    val status: String? = null,
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class AddUserRequest(
    @SerialName("fullname")
    val fullname: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("role")
    val role: String,
    @SerialName("wage")
    val wage: Double? = 0.0,
    @SerialName("status")
    val status: Int = 1,
    @SerialName("image")
    val image: String? = null
)

@Serializable
data class UpdateUserRequest(
    @SerialName("user_id")
    val userId: Int,
    @SerialName("fullname")
    val fullname: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("role")
    val role: String,
    @SerialName("wage")
    val wage: Double? = 0.0,
    @SerialName("status")
    val status: Int = 1,
    @SerialName("image")
    val image: String? = null
)

@Serializable
data class DeleteUserRequest(
    @SerialName("user_id")
    val userId: Int? = null
)