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

sealed class UpdateProductState {
    object Initial : UpdateProductState()
    object Loading : UpdateProductState()
    data class Success(val message: String) : UpdateProductState()
    data class Error(val message: String?) : UpdateProductState()
}

@HiltViewModel
class UpdateProductViewModel @Inject constructor(
    private val apiService: ProductApiService
) : ViewModel() {
    
    private val _state = MutableStateFlow<UpdateProductState>(UpdateProductState.Initial)
    val state: StateFlow<UpdateProductState> = _state
    
    private fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("onefood_prefs", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", null)
    }
    
    fun updateProduct(
        context: Context,
        productId: Int,
        name: String,
        description: String,
        category: String,
        price: Double,
        imageUrl: String,
        productActive: Int = 1
    ) {
        viewModelScope.launch {
            _state.value = UpdateProductState.Loading
            
            val token = getToken(context)
            if (token == null) {
                _state.value = UpdateProductState.Error("Không tìm thấy token đăng nhập. Vui lòng đăng nhập lại.")
                return@launch
            }
            
            try {
                val updateResponse = apiService.updateProduct(
                    productId = productId,
                    name = name,
                    description = description,
                    category = category,
                    price = price,
                    imageUrl = imageUrl,
                    productActive = productActive,
                    token = token
                )
                
                if (updateResponse.success == true) {
                    _state.value = UpdateProductState.Success(updateResponse.message ?: "Cập nhật sản phẩm thành công!")
                } else {
                    _state.value = UpdateProductState.Error(updateResponse.message ?: "Lỗi khi cập nhật sản phẩm")
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
                _state.value = UpdateProductState.Error(errorMessage)
            }
        }
    }
    
    fun updateProductWithImage(
        context: Context,
        productId: Int,
        imageBytes: ByteArray,
        fileName: String,
        name: String,
        description: String,
        category: String,
        price: Double,
        currentImageUrl: String
    ) {
        viewModelScope.launch {
            _state.value = UpdateProductState.Loading
            
            val token = getToken(context)
            if (token == null) {
                _state.value = UpdateProductState.Error("Không tìm thấy token đăng nhập. Vui lòng đăng nhập lại.")
                return@launch
            }
            
            try {
                var imageUrl = currentImageUrl
                
                // Upload new image if provided
                if (imageBytes.isNotEmpty()) {
                    val uploadResponse = apiService.uploadImage(imageBytes, fileName, token)
                    if (!uploadResponse.success) {
                        _state.value = UpdateProductState.Error(uploadResponse.message.ifEmpty { "Lỗi khi tải ảnh lên" })
                        return@launch
                    }
                    imageUrl = uploadResponse.imageUrl ?: currentImageUrl
                }
                
                // Update product
                val updateResponse = apiService.updateProduct(
                    productId = productId,
                    name = name,
                    description = description,
                    category = category,
                    price = price,
                    imageUrl = imageUrl,
                    productActive = 1,
                    token = token
                )
                
                if (updateResponse.success == true) {
                    _state.value = UpdateProductState.Success(updateResponse.message ?: "Cập nhật sản phẩm thành công!")
                } else {
                    _state.value = UpdateProductState.Error(updateResponse.message ?: "Lỗi khi cập nhật sản phẩm")
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
                _state.value = UpdateProductState.Error(errorMessage)
            }
        }
    }
    
    fun resetState() {
        _state.value = UpdateProductState.Initial
    }
}

