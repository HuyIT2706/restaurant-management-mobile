package com.example.onefood.main.home.model

data class RevenueItem(
    val id: Int,
    val name: String,
    val category: String,
    val quantity: Int,
    val revenue: Int,
    val bestSeller: Boolean,
    val imageRes: Int? = null, // Giữ lại cho fallback ảnh cục bộ
    val imageUrl: String? = null // ➕ Thêm dòng này
)
