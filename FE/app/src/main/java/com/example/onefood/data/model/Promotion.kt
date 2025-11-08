package com.example.onefood.main.home.ui

data class PromotionItem(
    val id: Int,
    val code: String,
    val startDate: String,
    val endDate: String,
    val status: Boolean,
    val discount: String,
    val quantity: Int,
    val discountType: String = "PhầnTrăm",     // MỚI
    val minOrderValue: String = "0 đ",        // MỚI
    val description: String = ""              // MỚI
)