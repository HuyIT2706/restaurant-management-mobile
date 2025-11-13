package com.example.onefood.main.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.api.PromotionApiService
import com.example.onefood.data.model.PromotionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromotionViewModel @Inject constructor(
    private val service: PromotionApiService
) : ViewModel() {

    private val _promotions = MutableStateFlow<List<PromotionItem>>(emptyList())
    val promotions: StateFlow<List<PromotionItem>> = _promotions

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // 🔹 Dùng để lưu khuyến mãi hiện tại (khi xem chi tiết hoặc chỉnh sửa)
    private val _currentPromotion = MutableStateFlow<PromotionItem?>(null)
    val currentPromotion: StateFlow<PromotionItem?> = _currentPromotion.asStateFlow()

    // 🔹 Load toàn bộ danh sách khuyến mãi
    fun loadPromotions(token: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val list = service.getPromotions(token)
                _promotions.value = list
            } catch (e: Exception) {
                e.printStackTrace()
                _promotions.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    // 🔹 Load chi tiết 1 khuyến mãi theo ID
    fun loadPromotionById(token: String, id: Int) {
        viewModelScope.launch {
            try {
                val promo = service.getPromotion(id, token)
                _currentPromotion.value = promo
            } catch (e: Exception) {
                e.printStackTrace()
                _currentPromotion.value = null
            }
        }
    }

    // 🔹 Xoá khuyến mãi
    fun deletePromotion(token: String, promoId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val ok = service.deletePromotion(token, promoId)
                if (ok) loadPromotions(token)
                onComplete(ok)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    // 🔹 Thêm khuyến mãi
    fun addPromotion(
        token: String,
        promoCode: String,
        promoType: String,
        promoValue: Double,
        promoQuantity: Int,
        promoDesc: String,
        promoMinOrderAmount: Double,
        promoStartDate: String,
        promoEndDate: String,
        onComplete: (Int?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val id = service.addPromotion(
                    token,
                    promoCode,
                    promoType,
                    promoValue,
                    promoQuantity,
                    promoDesc,
                    promoMinOrderAmount,
                    promoStartDate,
                    promoEndDate
                )
                if (id != null) loadPromotions(token)
                onComplete(id)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null)
            }
        }
    }

    // 🔹 Cập nhật khuyến mãi
    fun updatePromotion(
        token: String,
        promoId: Int,
        promoCode: String,
        promoType: String,
        promoValue: Double,
        promoQuantity: Int,
        promoDesc: String,
        promoMinOrderAmount: Double,
        promoStartDate: String,
        promoEndDate: String,
        promoActive: Int,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val ok = service.updatePromotion(
                    token,
                    promoId,
                    promoCode,
                    promoType,
                    promoValue,
                    promoQuantity,
                    promoDesc,
                    promoMinOrderAmount,
                    promoStartDate,
                    promoEndDate,
                    promoActive
                )
                if (ok) loadPromotions(token)
                onComplete(ok)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }
}
