package com.example.onefood.data.model

data class PromotionItem(
    val id: Int? = null,
    val code: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val status: Boolean? = null,
    val discount: String? = null,
    val quantity: Int? = null,
    val discountType: String? = null,     // "PhầnTrăm" hoặc "SốTiền"
    val minOrderValue: String? = null,    // Giá trị đơn hàng tối thiểu
    val description: String? = null        // Mô tả khuyến mãi
)