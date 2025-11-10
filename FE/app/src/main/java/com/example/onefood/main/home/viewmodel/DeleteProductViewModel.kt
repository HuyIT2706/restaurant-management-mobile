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

sealed class DeleteProductState {
    object Initial : DeleteProductState()
    object Loading : DeleteProductState()
    data class Success(val message: String) : DeleteProductState()
    data class Error(val message: String?) : DeleteProductState()
}

@HiltViewModel
class DeleteProductViewModel @Inject constructor(
    private val apiService: ProductApiService
) : ViewModel() {
    
    private val _state = MutableStateFlow<DeleteProductState>(DeleteProductState.Initial)
    val state: StateFlow<DeleteProductState> = _state
    
    private fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("onefood_prefs", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", null)
    }
    
    fun deleteProduct(context: Context, productId: Int) {
        viewModelScope.launch {
            _state.value = DeleteProductState.Loading
            
            val token = getToken(context)
            if (token == null) {
                _state.value = DeleteProductState.Error("Không tìm thấy token đăng nhập. Vui lòng đăng nhập lại.")
                return@launch
            }
            
            try {
                val deleteResponse = apiService.deleteProduct(productId, token)
                
                if (deleteResponse.success == true) {
                    _state.value = DeleteProductState.Success(deleteResponse.message ?: "Xóa sản phẩm thành công!")
                } else {
                    _state.value = DeleteProductState.Error(deleteResponse.message ?: "Lỗi khi xóa sản phẩm")
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
                _state.value = DeleteProductState.Error(errorMessage)
            }
        }
    }
    
    fun resetState() {
        _state.value = DeleteProductState.Initial
    }
}

