package com.example.onefood.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple in-memory cache for API responses
 * Cache expires after specified TTL (Time To Live)
 */
@javax.inject.Singleton
class ApiCache @javax.inject.Inject constructor() {
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val mutex = Mutex()
    
    data class CacheEntry(
        val data: Any,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    companion object {
        private const val DEFAULT_TTL = 5 * 60 * 1000L // 5 minutes
    }
    
    suspend fun <T> get(key: String, ttl: Long = DEFAULT_TTL): T? {
        return mutex.withLock {
            val entry = cache[key]
            if (entry != null && (System.currentTimeMillis() - entry.timestamp) < ttl) {
                @Suppress("UNCHECKED_CAST")
                entry.data as? T
            } else {
                cache.remove(key)
                null
            }
        }
    }
    
    suspend fun put(key: String, data: Any) {
        mutex.withLock {
            cache[key] = CacheEntry(data)
        }
    }
    
    suspend fun clear() {
        mutex.withLock {
            cache.clear()
        }
    }
    
    suspend fun remove(key: String) {
        mutex.withLock {
            cache.remove(key)
        }
    }
}

