package com.example.onefood.main.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.api.OrderApiService
import com.example.onefood.data.model.CreateOrderRequest
import com.example.onefood.data.model.OrderItemRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CreateOrderState {
    object Initial : CreateOrderState()
    object Loading : CreateOrderState()
    data class Success(val message: String, val orderId: Int, val totalAmount: Double) : CreateOrderState()
    data class Error(val message: String) : CreateOrderState()
}

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val apiService: OrderApiService
) : ViewModel() {
    
    private val _state = MutableStateFlow<CreateOrderState>(CreateOrderState.Initial)
    val state: StateFlow<CreateOrderState> = _state
    
    private fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("onefood_prefs", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", null)
    }
    
    fun createOrder(
        context: Context,
        tableId: Int,
        items: List<OrderItemRequest>
    ) {
        viewModelScope.launch {
            _state.value = CreateOrderState.Loading
            
            val token = getToken(context)
            if (token == null) {
                _state.value = CreateOrderState.Error("Không tìm thấy token đăng nhập. Vui lòng đăng nhập lại.")
                return@launch
            }
            
            if (items.isEmpty()) {
                _state.value = CreateOrderState.Error("Giỏ hàng trống. Vui lòng thêm món trước khi đặt hàng.")
                return@launch
            }
            
            try {
                val request = CreateOrderRequest(
                    tableId = tableId,
                    items = items
                )
                
                val response = apiService.createOrder(request, token)
                
                if (response.success == true) {
                    val orderId = response.orderId ?: 0
                    val totalAmount = response.totalAmount ?: 0.0
                    val message = response.message ?: "Đặt hàng thành công!"
                    _state.value = CreateOrderState.Success(message, orderId, totalAmount)
                } else {
                    _state.value = CreateOrderState.Error(response.message ?: "Lỗi khi đặt hàng")
                }
            } catch (e: Exception) {
                _state.value = CreateOrderState.Error(e.message ?: "Đã xảy ra lỗi khi đặt hàng")
            }
        }
    }
    
    fun resetState() {
        _state.value = CreateOrderState.Initial
    }
}

