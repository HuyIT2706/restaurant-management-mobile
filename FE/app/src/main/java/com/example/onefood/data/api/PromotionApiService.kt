package com.example.onefood.data.api

import com.example.onefood.data.model.PromotionItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

// Helpers to safely parse JSON primitives coming from backend which may be strings
private fun JsonElement?.asIntOrNull(): Int? = this?.jsonPrimitive?.content?.toIntOrNull()
private fun JsonElement?.asStringOrNull(): String? = this?.jsonPrimitive?.content
private fun JsonElement?.asBoolean(): Boolean {
    val c = this?.jsonPrimitive?.content?.lowercase() ?: return false
    return c == "1" || c == "true"
}

class PromotionApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {

    private fun extractJsonFromResponse(response: String): String {
        val trimmed = response.trim()
        val startIndex = trimmed.indexOfFirst { it == '{' }
        val lastIndex = trimmed.lastIndexOf('}')
        return if (startIndex >= 0 && lastIndex > startIndex) {
            trimmed.substring(startIndex, lastIndex + 1)
        } else trimmed
    }

    suspend fun getPromotions(token: String): List<PromotionItem> {
        val response = client.get("$baseUrl/promotions/api_promotion_list.php") {
            headers {
                append("Authorization", "Bearer $token")
                append("Accept", "application/json")
            }
        }
        val bodyText = response.body<String>()
        val jsonString = extractJsonFromResponse(bodyText)
        val json = Json.parseToJsonElement(jsonString).jsonObject
        val promotionsArray = json["promotions"]?.jsonArray

        return promotionsArray?.mapNotNull { elem ->
            try {
                val obj = elem.jsonObject
                val id = obj["promo_id"]?.asIntOrNull() ?: 0
                val code = obj["promo_code"]?.asStringOrNull() ?: ""
                val start = obj["promo_start_date"]?.asStringOrNull() ?: ""
                val end = obj["promo_end_date"]?.asStringOrNull() ?: ""
                val activeInt = obj["promo_active"]?.asIntOrNull() ?: 0
                val status = activeInt == 1
                val quantity = obj["promo_quantity"]?.asIntOrNull() ?: 0
                val type = obj["promo_type"]?.asStringOrNull() ?: "PhanTram"
                val value = obj["promo_value"]?.asStringOrNull() ?: "0"
                val minOrder = obj["promo_min_order_amount"]?.asStringOrNull() ?: "0"
                val desc = obj["promo_desc"]?.asStringOrNull() ?: ""

                val discount = if (type == "PhanTram") {
                    "$value%"
                } else {
                    val cleaned = value.replace(".0", "")
                    "$cleaned đ"
                }

                PromotionItem(
                    id = id.takeIf { it > 0 },
                    code = code.takeIf { it.isNotEmpty() },
                    startDate = start.takeIf { it.isNotEmpty() },
                    endDate = end.takeIf { it.isNotEmpty() },
                    status = status,
                    discount = discount.takeIf { it.isNotEmpty() },
                    quantity = quantity.takeIf { it > 0 },
                    discountType = if (type == "PhanTram") "PhầnTrăm" else "SốTiền",
                    minOrderValue = "$minOrder đ".takeIf { minOrder.isNotEmpty() },
                    description = desc.takeIf { it.isNotEmpty() }
                )
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()
    }

    suspend fun getPromotion(id: Int, token: String): PromotionItem? {
        val response = client.get("$baseUrl/promotions/api_promotion_get.php") {
            headers { append("Authorization", "Bearer $token") }
            parameter("id", id)
        }
        val bodyText = response.body<String>()
        val jsonString = extractJsonFromResponse(bodyText)
        val json = Json.parseToJsonElement(jsonString).jsonObject
        val success = json["success"].asBoolean()
        if (!success) return null

        val obj = json["promotion"]?.jsonObject ?: return null
        val pid = obj["promo_id"]?.asIntOrNull() ?: 0
        val code = obj["promo_code"]?.asStringOrNull() ?: ""
        val start = obj["promo_start_date"]?.asStringOrNull() ?: ""
        val end = obj["promo_end_date"]?.asStringOrNull() ?: ""
        val activeInt = obj["promo_active"]?.asIntOrNull() ?: 0
        val status = activeInt == 1
        val quantity = obj["promo_quantity"]?.asIntOrNull() ?: 0
        val type = obj["promo_type"]?.asStringOrNull() ?: "PhanTram"
        val value = obj["promo_value"]?.asStringOrNull() ?: "0"
        val minOrder = obj["promo_min_order_amount"]?.asStringOrNull() ?: "0"
        val desc = obj["promo_desc"]?.asStringOrNull() ?: ""

        val discount = if (type == "PhanTram") "$value%" else "$value đ"

        return PromotionItem(
            id = pid.takeIf { it > 0 },
            code = code.takeIf { it.isNotEmpty() },
            startDate = start.takeIf { it.isNotEmpty() },
            endDate = end.takeIf { it.isNotEmpty() },
            status = status,
            discount = discount.takeIf { it.isNotEmpty() },
            quantity = quantity.takeIf { it > 0 },
            discountType = if (type == "PhanTram") "PhầnTrăm" else "SốTiền",
            minOrderValue = "$minOrder đ".takeIf { minOrder.isNotEmpty() },
            description = desc.takeIf { it.isNotEmpty() }
        )
    }

    suspend fun addPromotion(
        token: String,
        promoCode: String,
        promoType: String,
        promoValue: Double,
        promoQuantity: Int,
        promoDesc: String,
        promoMinOrderAmount: Double,
        promoStartDate: String,
        promoEndDate: String
    ): Int? {
        val body = buildJsonObject {
            put("promo_code", promoCode)
            put("promo_type", promoType)
            put("promo_value", promoValue)
            put("promo_quantity", promoQuantity)
            put("promo_desc", promoDesc)
            put("promo_min_order_amount", promoMinOrderAmount)
            put("promo_start_date", promoStartDate)
            put("promo_end_date", promoEndDate)
        }

        val response = client.post("$baseUrl/promotions/api_promotion_add.php") {
            headers { append("Authorization", "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(body.toString())
        }

        val bodyText = response.body<String>()
        val jsonString = extractJsonFromResponse(bodyText)
        val json = Json.parseToJsonElement(jsonString).jsonObject
        val success = json["success"].asBoolean()
        return if (success) json["promo_id"]?.asIntOrNull() else null
    }

    suspend fun updatePromotion(
        token: String,
        promoId: Int,
        promoCode: String,
        promoType: String,
        promoValue: Double,
        promoQuantity: Int,
        promoDesc: String,
        promoMinOrderAmount: Double,
        promoStartDate: String,
        promoEndDate: String,
        promoActive: Int
    ): Boolean {
        val body = buildJsonObject {
            put("promo_id", promoId)
            put("promo_code", promoCode)
            put("promo_type", promoType)
            put("promo_value", promoValue)
            put("promo_quantity", promoQuantity)
            put("promo_desc", promoDesc)
            put("promo_min_order_amount", promoMinOrderAmount)
            put("promo_start_date", promoStartDate)
            put("promo_end_date", promoEndDate)
            put("promo_active", promoActive)
        }

        val response = client.put("$baseUrl/promotions/api_promotion_update.php") {
            headers { append("Authorization", "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(body.toString())
        }

        val bodyText = response.body<String>()
        val jsonString = extractJsonFromResponse(bodyText)
        val json = Json.parseToJsonElement(jsonString).jsonObject
        return json["success"].asBoolean()
    }

    // ✅ FIXED deletePromotion cho PHP nhận được id
// ✅ FIXED — gửi id trực tiếp trong URL thay vì dùng parameters.append()
    suspend fun deletePromotion(token: String, promoId: Int): Boolean {
        val response = client.request("$baseUrl/promotions/api_promotion_delete.php?id=$promoId") {
            method = HttpMethod.Delete
            headers {
                append("Authorization", "Bearer $token")
                append("Accept", "application/json")
            }
        }

        val bodyText = response.body<String>()
        val jsonString = extractJsonFromResponse(bodyText)
        val json = Json.parseToJsonElement(jsonString).jsonObject
        return json["success"].asBoolean()
    }


}