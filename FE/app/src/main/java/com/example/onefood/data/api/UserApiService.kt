package com.example.onefood.data.api

import com.example.onefood.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull

class UserApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    // Helper function to extract JSON from response
    private fun extractJsonFromResponse(response: String): String {
        val trimmed = response.trim()
        val startIndex = trimmed.indexOfFirst { it == '{' }
        val lastIndex = trimmed.lastIndexOf('}')
        
        return if (startIndex >= 0 && lastIndex > startIndex) {
            trimmed.substring(startIndex, lastIndex + 1)
        } else {
            trimmed
        }
    }
    
    /**
     * Lấy danh sách tất cả nhân viên
     */
    suspend fun getUsers(token: String): GetUsersResponse {
        val responseText = client.get("$baseUrl/users/api_get_account.php") {
            headers {
                append("Authorization", "Bearer $token")
            }
        }.body<String>()

        val jsonString = extractJsonFromResponse(responseText)

        // Try to parse responsively: backend may return different shapes when token missing
        val parser = Json { ignoreUnknownKeys = true }
        try {
            val elem = parser.parseToJsonElement(jsonString).jsonObject
            // If backend uses {"success": false, "message": "..."}
            if (elem.containsKey("success")) {
                val success = elem["success"]?.jsonPrimitive?.booleanOrNull ?: false
                if (!success) {
                    val msg = try { elem["message"]?.jsonPrimitive?.content } catch (e: Exception) { null } ?: "Lỗi khi lấy dữ liệu"
                    throw Exception(msg)
                }
            }
            // Otherwise decode to expected response
            return parser.decodeFromString(GetUsersResponse.serializer(), jsonString)
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * Thêm nhân viên mới
     */
    suspend fun addUser(
        fullname: String,
        phone: String,
        gender: String?,
        role: String,
        wage: Double?,
        status: Int,
        image: String?,
        token: String
    ): AddUserResponse {
        val request = AddUserRequest(
            fullname = fullname,
            phone = phone,
            gender = gender,
            role = role,
            wage = wage ?: 0.0,
            status = status,
            image = image
        )
        
        val jsonBody = kotlinx.serialization.json.Json.encodeToString(AddUserRequest.serializer(), request)
        
        val responseText = client.post("$baseUrl/users/api_add_account.php") {
            headers {
                append("Authorization", "Bearer $token")
            }
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }.body<String>()
        
        val jsonString = extractJsonFromResponse(responseText)
        val parser = Json { ignoreUnknownKeys = true }
        return parser.decodeFromString(AddUserResponse.serializer(), jsonString)
    }
    
    /**
     * Cập nhật thông tin nhân viên
     */
    suspend fun updateUser(
        userId: Int,
        fullname: String,
        phone: String,
        gender: String?,
        role: String,
        wage: Double?,
        status: Int,
        image: String?,
        token: String
    ): UpdateUserResponse {
        val request = UpdateUserRequest(
            userId = userId,
            fullname = fullname,
            phone = phone,
            gender = gender,
            role = role,
            wage = wage ?: 0.0,
            status = status,
            image = image
        )
        
        val jsonBody = kotlinx.serialization.json.Json.encodeToString(UpdateUserRequest.serializer(), request)
        
        val responseText = client.post("$baseUrl/users/api_update_account.php") {
            headers {
                append("Authorization", "Bearer $token")
            }
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }.body<String>()
        
        val jsonString = extractJsonFromResponse(responseText)
        val parser = Json { ignoreUnknownKeys = true }
        return parser.decodeFromString(UpdateUserResponse.serializer(), jsonString)
    }
    
    /**
     * Xoá nhân viên
     */
    suspend fun deleteUser(
        userId: Int,
        token: String
    ): DeleteUserResponse {
        val request = DeleteUserRequest(userId = userId)
        val jsonBody = kotlinx.serialization.json.Json.encodeToString(DeleteUserRequest.serializer(), request)
        
        val responseText = client.post("$baseUrl/users/api_delete_account.php") {
            headers {
                append("Authorization", "Bearer $token")
            }
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }.body<String>()
        
        val jsonString = extractJsonFromResponse(responseText)
        val parser = Json { ignoreUnknownKeys = true }
        return parser.decodeFromString(DeleteUserResponse.serializer(), jsonString)
    }
}
