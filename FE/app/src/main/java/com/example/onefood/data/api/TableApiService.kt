package com.example.onefood.data.api

import com.example.onefood.data.model.TableResponse
import retrofit2.http.GET

interface TableApiService {
    @GET("tables/tables.php")
    suspend fun getTables(): TableResponse
}