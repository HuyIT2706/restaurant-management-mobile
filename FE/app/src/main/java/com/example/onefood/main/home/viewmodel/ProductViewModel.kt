package com.example.onefood.main.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.model.Product
import com.example.onefood.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getProducts()
                .catch { e ->
                    _error.value = "Error: ${e.message ?: e.javaClass.simpleName}"
                    _isLoading.value = false
                }
                .collect { products ->
                    _products.value = products
                    _isLoading.value = false
                }
        }
    }

    fun refreshProducts() {
        viewModelScope.launch {
            repository.clearCache()
            loadProducts()
        }
    }
}

