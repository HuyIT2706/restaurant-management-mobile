package com.example.onefood.data.repository

import com.example.onefood.data.api.ProductApiService
import com.example.onefood.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val api: ProductApiService
) {
    fun getProducts(): Flow<List<Product>> = flow {
        try {
            val products = api.getProducts()
            emit(products)
        } catch (e: Exception) {
            throw Exception("Failed to fetch products: ${e.message}", e)
        }
    }
}

