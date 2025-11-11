package com.example.onefood.data.model

import com.squareup.moshi.Json

data class SelectTableResponse(
    val success: Boolean?,
    val message: String?,
    @Json(name = "table_id")
    val tableId: Int? = null,
    @Json(name = "new_status")
    val newStatus: String? = null
)

