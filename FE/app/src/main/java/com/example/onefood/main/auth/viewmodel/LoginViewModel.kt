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
                // Dùng IP LAN và đường dẫn đúng tới thư mục trong htdocs
                val baseUrl = "http://10.111.17.241/BeMobie/restaurant-management-mobile/BE/login.php"


                val respText = client.post(baseUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("phone" to phone, "password" to password))
                }.body<String>()

                // Log response thô để debug
                android.util.Log.d("LoginVM", "Raw response: $respText")

                // Config parser chấp nhận JSON không chuẩn
                val parser = Json { isLenient = true }
                val json = parser.parseToJsonElement(respText).jsonObject
                val success = json["success"]?.jsonPrimitive?.content?.toBoolean() ?: false
                val message = json["message"]?.jsonPrimitive?.content
                val token = json["token"]?.jsonPrimitive?.content
                val role = json["role"]?.jsonPrimitive?.content

                if (success) {
                    token?.let { t ->
                        val prefs = context.getSharedPreferences("onefood_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("jwt_token", t).apply()
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
