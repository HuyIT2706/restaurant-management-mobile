package com.example.onefood.data.models

import androidx.annotation.DrawableRes

data class RevenueItem(
    val id: Int,
    val name: String,
    val category: String,
    val quantity: Int,
    val revenue: Int,
    val bestSeller: Boolean,
    @DrawableRes val imageRes: Int
)
