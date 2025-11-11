package com.example.onefood.main.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.api.OrderApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val apiService: OrderApiService
) : ViewModel() {
    
    private val _isPaid = MutableStateFlow(false)
    val isPaid: StateFlow<Boolean> = _isPaid.asStateFlow()
    
    private val _promotionCode = MutableStateFlow("")
    val promotionCode: StateFlow<String> = _promotionCode.asStateFlow()
    
    private val _selectedPaymentMethod = MutableStateFlow("Ngân hàng")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()
    
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Initial)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    private val _vnpayUrl = MutableStateFlow<String?>(null)
    val vnpayUrl: StateFlow<String?> = _vnpayUrl.asStateFlow()

    private fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("onefood_prefs", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", null)
    }
    
    fun updatePromotionCode(code: String) {
        _promotionCode.value = code
    }
    
    fun updatePaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }
    
    fun calculateDiscount(totalAmount: Int, code: String): Int {
        // TODO: Implement actual discount calculation based on promotion code
        // For now, return 0 or a simple calculation
        if (code.isBlank()) {
            return 0
        }
        // Simple example: if code is "DISCOUNT10", apply 10% discount
        // This should be replaced with actual API call to validate and calculate discount
        return when (code.uppercase()) {
            "DISCOUNT10" -> (totalAmount * 0.1).toInt()
            "DISCOUNT20" -> (totalAmount * 0.2).toInt()
            else -> 0
        }
    }
    
    fun processPayment(context: Context, orderId: String) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            val numericId = orderId.toIntOrNull()
            if (numericId == null) {
                _paymentState.value = PaymentState.Error("Mã đơn hàng không hợp lệ.")
                return@launch
            }
            val token = getToken(context)
            if (token == null) {
                _paymentState.value = PaymentState.Error("Không tìm thấy token đăng nhập. Vui lòng đăng nhập lại.")
                return@launch
            }
            try {
                val message = apiService.payOrderWithCash(numericId, token)
                _isPaid.value = true
                _paymentState.value = PaymentState.Success(message)
            } catch (e: Exception) {
                _isPaid.value = false
                _paymentState.value = PaymentState.Error(e.message ?: "Không thể thanh toán tiền mặt.")
            }
        }
    }

    fun createVnPayUrl(context: Context, orderId: String, bankCode: String = "NCB") {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            val numericId = orderId.toIntOrNull()
            if (numericId == null) {
                _paymentState.value = PaymentState.Error("Mã đơn hàng không hợp lệ.")
                return@launch
            }
            val token = getToken(context)
            if (token == null) {
                _paymentState.value = PaymentState.Error("Không tìm thấy token đăng nhập. Vui lòng đăng nhập lại.")
                return@launch
            }
            try {
                val url = apiService.createVnPayUrl(numericId, token, bankCode)
                _vnpayUrl.value = url
                _paymentState.value = PaymentState.Initial
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error(e.message ?: "Không thể tạo thanh toán VNPay.")
            }
        }
    }
    
    fun clearVnPayUrl() {
        _vnpayUrl.value = null
    }
    
    fun resetState() {
        _isPaid.value = false
        _promotionCode.value = ""
        _selectedPaymentMethod.value = "Ngân hàng"
        _paymentState.value = PaymentState.Initial
        _vnpayUrl.value = null
    }
}

sealed class PaymentState {
    object Initial : PaymentState()
    object Loading : PaymentState()
    data class Success(val message: String) : PaymentState()
    data class Error(val message: String) : PaymentState()
}

