package com.example.onefood.main.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.api.OrderApiService
import com.example.onefood.data.model.PromotionValidation
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
    
    private val _validatedPromotion = MutableStateFlow<PromotionValidation?>(null)
    val validatedPromotion: StateFlow<PromotionValidation?> = _validatedPromotion.asStateFlow()
    
    private val _discountAmount = MutableStateFlow(0)
    val discountAmount: StateFlow<Int> = _discountAmount.asStateFlow()
    
    private val _promotionValidationError = MutableStateFlow<String?>(null)
    val promotionValidationError: StateFlow<String?> = _promotionValidationError.asStateFlow()
    
    private val _isValidatingPromotion = MutableStateFlow(false)
    val isValidatingPromotion: StateFlow<Boolean> = _isValidatingPromotion.asStateFlow()
    
    private val _availablePromotions = MutableStateFlow<List<PromotionValidation>>(emptyList())
    val availablePromotions: StateFlow<List<PromotionValidation>> = _availablePromotions.asStateFlow()
    
    private val _isLoadingPromotions = MutableStateFlow(false)
    val isLoadingPromotions: StateFlow<Boolean> = _isLoadingPromotions.asStateFlow()
    
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
        // Clear validation when code changes
        if (code.isBlank()) {
            _validatedPromotion.value = null
            _discountAmount.value = 0
            _promotionValidationError.value = null
        }
    }
    
    fun loadAvailablePromotions(context: Context, orderAmount: Int) {
        viewModelScope.launch {
            _isLoadingPromotions.value = true
            val token = getToken(context)
            if (token == null) {
                _isLoadingPromotions.value = false
                return@launch
            }
            
            try {
                val promotions = apiService.getAvailablePromotions(orderAmount, token)
                _availablePromotions.value = promotions
            } catch (e: Exception) {
                // Silently fail - don't show error if can't load promotions
                _availablePromotions.value = emptyList()
            } finally {
                _isLoadingPromotions.value = false
            }
        }
    }
    
    fun selectPromotion(promotion: PromotionValidation) {
        _promotionCode.value = promotion.promo_code ?: ""
        _validatedPromotion.value = promotion
        _discountAmount.value = promotion.discount_amount?.toInt() ?: 0
        _promotionValidationError.value = null
    }
    
    fun validatePromotionCode(context: Context, totalAmount: Int) {
        val code = _promotionCode.value.trim()
        if (code.isBlank()) {
            _validatedPromotion.value = null
            _discountAmount.value = 0
            _promotionValidationError.value = null
            return
        }
        
        viewModelScope.launch {
            _isValidatingPromotion.value = true
            _promotionValidationError.value = null
            
            val token = getToken(context)
            if (token == null) {
                _promotionValidationError.value = "Không tìm thấy token đăng nhập. Vui lòng đăng nhập lại."
                _isValidatingPromotion.value = false
                return@launch
            }
            
            try {
                val promotion = apiService.validatePromotionCode(code, totalAmount, token)
                _validatedPromotion.value = promotion
                _discountAmount.value = promotion.discount_amount?.toInt() ?: 0
                _promotionValidationError.value = null
            } catch (e: Exception) {
                _validatedPromotion.value = null
                _discountAmount.value = 0
                _promotionValidationError.value = e.message ?: "Lỗi validate mã khuyến mãi."
            } finally {
                _isValidatingPromotion.value = false
            }
        }
    }
    
    fun updatePaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }
    
    fun calculateDiscount(totalAmount: Int): Int {
        return _discountAmount.value
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
                val promoCode = _promotionCode.value.trim().takeIf { it.isNotBlank() }
                val promoId = _validatedPromotion.value?.promo_id
                val message = apiService.payOrderWithCash(numericId, token, promoCode, promoId)
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
                val promoCode = _promotionCode.value.trim().takeIf { it.isNotBlank() }
                val promoId = _validatedPromotion.value?.promo_id
                val url = apiService.createVnPayUrl(numericId, token, bankCode, promoCode, promoId)
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
        _validatedPromotion.value = null
        _discountAmount.value = 0
        _promotionValidationError.value = null
        _isValidatingPromotion.value = false
        _availablePromotions.value = emptyList()
        _isLoadingPromotions.value = false
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

