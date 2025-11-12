package com.example.onefood.main.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.api.StatisticsApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val service: StatisticsApiService
) : ViewModel() {

    private val _revenueJson = MutableStateFlow<String?>(null)
    val revenueJson: StateFlow<String?> = _revenueJson

    private val _productDetailsJson = MutableStateFlow<String?>(null)
    val productDetailsJson: StateFlow<String?> = _productDetailsJson

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadRevenue(token: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val res = service.getRevenueByProduct(token)
                _revenueJson.value = res
            } catch (e: Exception) {
                _revenueJson.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadProductDetails(productId: Int, token: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val res = service.getProductDetails(productId, token)
                _productDetailsJson.value = res
            } catch (e: Exception) {
                _productDetailsJson.value = null
            } finally {
                _loading.value = false
            }
        }
    }
}
