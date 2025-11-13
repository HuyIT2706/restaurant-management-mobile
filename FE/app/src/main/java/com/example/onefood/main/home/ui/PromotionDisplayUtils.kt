package com.example.onefood.main.home.ui

import androidx.compose.ui.graphics.Color
import com.example.onefood.data.model.PromotionItem
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.Normalizer
import java.text.NumberFormat
import java.util.Locale

private val combiningAccentRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
private val viLocale = Locale("vi", "VN")
private val currencyFormatter: NumberFormat = NumberFormat.getNumberInstance(viLocale).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 0
}
private val percentFormatter = DecimalFormat("#.##")

internal fun normalizePromotionInput(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(combiningAccentRegex, "")
        .replace("\\s+".toRegex(), "")
        .lowercase(Locale.getDefault())
}

internal fun parsePromotionBigDecimal(raw: String?): BigDecimal? {
    if (raw.isNullOrBlank()) return null
    val sanitized = Normalizer.normalize(raw, Normalizer.Form.NFD)
        .replace(combiningAccentRegex, "")
        .replace("đ", "", ignoreCase = true)
        .replace("d", "", ignoreCase = true)
        .replace("%", "")
        .replace(",", "")
        .trim()
    if (sanitized.isEmpty()) return null
    return sanitized.toBigDecimalOrNull()
}

private fun formatCurrency(raw: String?): String {
    val amount = parsePromotionBigDecimal(raw) ?: return raw?.trim()?.ifEmpty { "0 đ" } ?: "0 đ"
    val scaled = amount.setScale(0, RoundingMode.HALF_UP)
    return currencyFormatter.format(scaled) + " đ"
}

private fun formatPercent(raw: String?): String {
    val amount = parsePromotionBigDecimal(raw) ?: return raw?.trim()?.ifEmpty { "0%" } ?: "0%"
    return percentFormatter.format(amount) + "%"
}

fun formatPromotionDiscount(promotion: PromotionItem): String {
    val type = normalizePromotionInput(promotion.discountType)
    return when {
        type.contains("phantram") || type.contains("percent") -> formatPercent(promotion.discount)
        type.contains("sotien") || type.contains("amount") -> formatCurrency(promotion.discount)
        else -> promotion.discount?.trim()?.ifEmpty { "-" } ?: "-"
    }
}

fun formatPromotionMinOrder(promotion: PromotionItem): String {
    return formatCurrency(promotion.minOrderValue)
}

fun extractPromotionNumericString(raw: String?): String {
    val amount = parsePromotionBigDecimal(raw)?.stripTrailingZeros() ?: return raw?.trim() ?: ""
    return amount.toPlainString()
}

fun formatPromotionDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "--/--/----"
    val datePart = raw.take(10)
    val segments = datePart.split("-")
    return if (segments.size == 3) {
        "${segments[2]}/${segments[1]}/${segments[0]}"
    } else raw
}

fun formatPromotionTypeLabel(type: String?): String {
    val normalized = normalizePromotionInput(type)
    return when {
        normalized.contains("phantram") || normalized.contains("percent") -> "Giảm theo %"
        normalized.contains("sotien") || normalized.contains("amount") -> "Giảm theo số tiền"
        else -> type?.ifBlank { "Không xác định" } ?: "Không xác định"
    }
}

fun canonicalPromotionType(type: String?): String {
    val normalized = normalizePromotionInput(type)
    return when {
        normalized.contains("sotien") || normalized.contains("amount") -> "SoTien"
        else -> "PhanTram"
    }
}

fun promotionStatusLabel(promotion: PromotionItem): String =
    if (promotion.status == true) "Hoạt động" else "Không hoạt động"

fun promotionStatusColor(promotion: PromotionItem): Color =
    if (promotion.status == true) Color(0xFF4CAF50) else Color(0xFFD32F2F)

