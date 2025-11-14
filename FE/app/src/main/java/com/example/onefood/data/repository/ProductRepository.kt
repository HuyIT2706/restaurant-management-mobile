package com.example.onefood.data.repository

import com.example.onefood.data.api.ProductApiService
import com.example.onefood.data.cache.ApiCache
import com.example.onefood.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val api: ProductApiService,
    private val cache: ApiCache
) {
    companion object {
        private const val CACHE_KEY_PRODUCTS = "products_list"
        private const val CACHE_TTL = 2 * 60 * 1000L // 2 minutes
    }
    
    fun getProducts(forceRefresh: Boolean = false): Flow<List<Product>> = flow {
        try {
            // Try to get from cache first
            if (!forceRefresh) {
                val cached = cache.get<List<Product>>(CACHE_KEY_PRODUCTS, CACHE_TTL)
                if (cached != null) {
                    emit(cached)
                    return@flow
                }
            }
            
            // Fetch from API
            val products = api.getProducts()
            
            // Cache the result
            cache.put(CACHE_KEY_PRODUCTS, products)
            
            emit(products)
        } catch (e: Exception) {
            // On error, try to return cached data if available
            val cached = cache.get<List<Product>>(CACHE_KEY_PRODUCTS, CACHE_TTL * 2)
            if (cached != null) {
                emit(cached)
            } else {
                throw Exception("Failed to fetch products: ${e.message}", e)
            }
        }
    }
    
    suspend fun clearCache() {
        cache.remove(CACHE_KEY_PRODUCTS)
    }
}

