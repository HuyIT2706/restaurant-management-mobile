package com.example.onefood.main.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.api.ProductApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddProductState {
    object Initial : AddProductState()
    object Loading : AddProductState()
    data class Success(val message: String) : AddProductState()
    data class Error(val message: String?) : AddProductState()
}

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val apiService: ProductApiService
) : ViewModel() {
    
    private val _state = MutableStateFlow<AddProductState>(AddProductState.Initial)
    val state: StateFlow<AddProductState> = _state
    
    private fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("onefood_prefs", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", null)
    }
    
    fun uploadImageAndAddProduct(
        context: Context,
        imageBytes: ByteArray?,
        fileName: String,
        name: String,
        description: String,
        category: String,
        price: Double
    ) {
        viewModelScope.launch {
            _state.value = AddProductState.Loading
            
            val token = getToken(context)
            if (token == null) {
                _state.value = AddProductState.Error("Không tìm thấy token đăng nhập. Vui lòng đăng nhập lại.")
                return@launch
            }
            
            try {
                var imageUrl = ""
                var uploadWarning: String? = null
                
                // Upload image if provided
                if (imageBytes != null && imageBytes.isNotEmpty()) {
                    try {
                        val uploadResponse = apiService.uploadImage(imageBytes, fileName, token)
                        if (uploadResponse.success) {
                            imageUrl = uploadResponse.imageUrl ?: ""
                        } else {
                            uploadWarning = uploadResponse.message.ifEmpty { "Không thể tải ảnh lên, sẽ thêm sản phẩm không có ảnh." }
                        }
                    } catch (e: Exception) {
                        uploadWarning = "Không thể tải ảnh lên: ${e.localizedMessage ?: e.message ?: "Lỗi không xác định"}. Sản phẩm vẫn được thêm không có ảnh."
                    }
                }
                
                // Add product
                val addResponse = apiService.addProduct(
                    name = name,
                    description = description,
                    category = category,
                    price = price,
                    imageUrl = imageUrl,
                    token = token
                )
                
                if (addResponse.success == true) {
                    val successMessage = buildString {
                        append(addResponse.message ?: "Thêm sản phẩm thành công!")
                        if (!uploadWarning.isNullOrBlank()) {
                            append("\n")
                            append(uploadWarning)
                        }
                    }
                    _state.value = AddProductState.Success(successMessage)
                } else {
                    _state.value = AddProductState.Error(addResponse.message ?: "Lỗi khi thêm sản phẩm")
                }
            } catch (e: Exception) {
                // Extract meaningful error message
                val errorMessage = when {
                    e.message?.contains("Failed to parse JSON") == true -> {
                        "Lỗi định dạng dữ liệu từ server. Vui lòng thử lại."
                    }
                    e.message?.contains("Failed to call API") == true -> {
                        "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng."
                    }
                    e.message?.contains("401") == true || e.message?.contains("403") == true -> {
                        "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                    }
                    else -> {
                        "Lỗi: ${e.localizedMessage ?: e.message ?: "Đã xảy ra lỗi không xác định"}"
                    }
                }
                _state.value = AddProductState.Error(errorMessage)
            }
        }
    }
    
    fun resetState() {
        _state.value = AddProductState.Initial
    }
}

