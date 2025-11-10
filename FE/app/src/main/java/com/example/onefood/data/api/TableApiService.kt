package com.example.onefood.data.api

import com.example.onefood.data.model.SelectTableRequest
import com.example.onefood.data.model.SelectTableResponse
import com.example.onefood.data.model.TableResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TableApiService {
    @GET("tables/tables.php")
    suspend fun getTables(): TableResponse
    
    @POST("tables/select_table.php")
    suspend fun selectTable(@Body request: SelectTableRequest): SelectTableResponse
}