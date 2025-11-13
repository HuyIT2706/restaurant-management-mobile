package com.example.onefood.main.auth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.ktor.client.call.body
import kotlinx.serialization.json.Json
import android.util.Base64
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Simple sealed state to match LoginScreen usage
sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    data class Success(val message: String, val role: String = "") : LoginState()
    data class Error(val message: String) : LoginState()
}


class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    fun login(phone: String, password: String, context: Context) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val baseUrl = "https://onefood.id.vn/BE/login.php"

                val respText = client.post(baseUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("phone" to phone, "password" to password))
                }.body<String>()

                val json = Json.parseToJsonElement(respText).jsonObject
                val success = json["success"]?.jsonPrimitive?.content?.toBoolean() ?: false
                val message = json["message"]?.jsonPrimitive?.content
                val token = json["token"]?.jsonPrimitive?.content
                val role = json["role"]?.jsonPrimitive?.content

                if (success) {
                    token?.let { t ->
                        val prefs = context.getSharedPreferences("onefood_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("jwt_token", t).apply()
                        // Try to decode JWT payload to save some user info locally (name/phone/user_id/role)
                        try {
                            val parts = t.split('.')
                            if (parts.size >= 2) {
                                val payload = parts[1]
                                // Decode base64 URL-safe payload. Use android.util.Base64 to support lower API levels.
                                val decodedBytes = try {
                                    Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                                } catch (ex: IllegalArgumentException) {
                                    // Fallback in case of unexpected padding/format
                                    Base64.decode(payload, Base64.DEFAULT)
                                }
                                val payloadJson = String(decodedBytes, Charsets.UTF_8)
                                val obj = Json.parseToJsonElement(payloadJson).jsonObject
                                val data = obj["data"]?.jsonObject
                                // Safely extract fields as strings and convert where needed
                                val userId = try {
                                    data?.get("user_id")?.jsonPrimitive?.content?.toIntOrNull()
                                } catch (e: Exception) {
                                    null
                                }
                                val userRole = try {
                                    data?.get("user_role")?.jsonPrimitive?.content
                                } catch (e: Exception) {
                                    null
                                }
                                val phoneFromToken = try {
                                    data?.get("phone")?.jsonPrimitive?.content
                                } catch (e: Exception) {
                                    null
                                }
                                val nameFromToken = try {
                                    data?.get("name")?.jsonPrimitive?.content
                                } catch (e: Exception) {
                                    null
                                }
                                userId?.let { prefs.edit().putInt("user_id", it).apply() }
                                userRole?.let { prefs.edit().putString("user_role", it).apply() }
                                phoneFromToken?.let { prefs.edit().putString("user_phone", it).apply() }
                                nameFromToken?.let { prefs.edit().putString("user_name", it).apply() }
                            }
                        } catch (e: Exception) {
                            // ignore decode errors
                        }
                    }
                    _loginState.value = LoginState.Success(message ?: "Đăng nhập thành công", role ?: "")
                } else {
                    _loginState.value = LoginState.Error(message ?: "Đăng nhập thất bại")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Lỗi kết nối: ${e.localizedMessage}")
            }
        }
    }

    fun messageShown() {
        _loginState.value = LoginState.Initial
    }
}