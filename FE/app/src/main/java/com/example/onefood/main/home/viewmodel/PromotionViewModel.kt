package com.example.onefood.main.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.api.PromotionApiService
import com.example.onefood.data.model.PromotionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun loadPromotions(token: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val list = service.getPromotions(token)
                _promotions.value = list
            } catch (e: Exception) {
                // log or handle
                _promotions.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun deletePromotion(token: String, promoId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val ok = service.deletePromotion(token, promoId)
                if (ok) loadPromotions(token)
                onComplete(ok)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

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
                onComplete(null)
            }
        }
    }

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
                onComplete(false)
            }
        }
    }
}
