package com.example.onefood.data.model

data class Table(
    val id: Int?,
    val name: String?,
    val status: String?
)

data class TableResponse(
    val success: Boolean?,
    val count: Int?,
    val data: List<Table>?
)