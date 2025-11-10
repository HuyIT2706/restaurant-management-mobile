package com.example.onefood.data.model

import com.squareup.moshi.Json

data class SelectTableRequest(
    @Json(name = "table_id")
    val tableId: Int?
)

