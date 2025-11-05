package com.example.onefood.data.repository

import com.example.onefood.data.api.TableApiService
import com.example.onefood.data.model.Table
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TableRepository @Inject constructor(
    private val api: TableApiService
) {
    fun getTables(): Flow<List<Table>> = flow {
        try {
            val response = api.getTables()
            if (response.success == true) {
                val tables = response.data.orEmpty()
                if (tables.isEmpty()) {
                    throw Exception("No tables found in response")
                }
                emit(tables)
            } else {
                throw Exception("API returned success=false. Count: ${response.count}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch tables: ${e.message}", e)
        }
    }
}