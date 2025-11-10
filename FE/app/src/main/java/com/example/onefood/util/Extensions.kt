package com.example.onefood.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Shared NumberFormat instance for price formatting (thread-safe and reusable)
 */
private val priceFormatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))

/**
 * Extension function to format Int price to Vietnamese currency format
 * Uses NumberFormat for better performance than regex
 */
fun Int.formatPrice(): String {
    return "${priceFormatter.format(this)} ₫"
}

/**
 * Extension function to format Double price to Vietnamese currency format
 */
fun Double.formatPrice(): String {
    return "${priceFormatter.format(this)} ₫"
}

