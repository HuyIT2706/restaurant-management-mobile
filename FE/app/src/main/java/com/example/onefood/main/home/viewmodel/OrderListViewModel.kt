package com.example.onefood.main.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.api.OrderApiService
import com.example.onefood.data.model.OrderListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderListViewModel @Inject constructor(
    private val apiService: OrderApiService
) : ViewModel() {
    
    private val _orders = MutableStateFlow<List<OrderListItem>>(emptyList())
    val orders: StateFlow<List<OrderListItem>> = _orders
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("onefood_prefs", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", null)
    }
    
    fun loadOrders(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val token = getToken(context)
            if (token == null) {
                _error.value = "Không tìm thấy token đăng nhập. Vui lòng đăng nhập lại."
                _isLoading.value = false
                return@launch
            }
            
            try {
                val ordersList = apiService.getOrderList(token)
                _orders.value = ordersList
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Đã xảy ra lỗi khi tải danh sách đơn hàng"
                _isLoading.value = false
            }
        }
    }
    
    fun refreshOrders(context: Context) {
        loadOrders(context)
    }
}

