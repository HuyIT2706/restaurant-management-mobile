package com.example.onefood.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PromotionValidationResponse(
    val success: Boolean,
    val message: String? = null,
    val promotion: PromotionValidation? = null
)

@Serializable
data class PromotionValidation(
    val promo_id: Int?,
    val promo_code: String?,
    val promo_type: String?,
    val promo_value: Double?,
    val promo_desc: String? = null,
    val discount_amount: Double?,
    val final_amount: Double?
)

