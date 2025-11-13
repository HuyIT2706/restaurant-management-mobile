package com.example.onefood.data.model

data class RevenueItem(
    val id: Int? = null,
    val name: String? = null,              // Tên sản phẩm
    val category: String? = null,          // Danh mục sản phẩm
    val quantity: Int? = null,             // Số lượng đã bán
    val revenue: Int? = null,              // Doanh thu
    val bestSeller: Boolean? = null,        // Có phải sản phẩm bán chạy không
    val imageRes: Int? = null,             // Resource ID của ảnh (nếu dùng local)
    val imageUrl: String? = null           // URL ảnh từ server
)