package com.example.onefood.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*

// ----------------- Helper Functions -----------------
private fun JsonElement?.asIntOrNull(): Int? = this?.jsonPrimitive?.content?.toIntOrNull()
private fun JsonElement?.asStringOrNull(): String? = this?.jsonPrimitive?.content
private fun JsonElement?.asBoolean(): Boolean {
    val c = this?.jsonPrimitive?.content?.lowercase() ?: return false
    return c == "1" || c == "true"
}

// ----------------- StatisticsApiService -----------------
class StatisticsApiService(
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

    // ✅ Lấy danh sách doanh thu theo sản phẩm
    suspend fun getRevenueByProduct(token: String): String {
        val response = client.get("${baseUrl}statistials/api_statistics_revenue.php") {
            headers {
                append("Authorization", "Bearer $token")
                append("Accept", "application/json")
            }
        }
        val bodyText = response.body<String>()
        return extractJsonFromResponse(bodyText)
    }

    // ✅ Lấy chi tiết doanh thu của 1 sản phẩm
    suspend fun getProductDetails(productId: Int, token: String): String {
        val response = client.get("${baseUrl}statistials/api_statistics_detail.php") {
            headers {
                append("Authorization", "Bearer $token")
                append("Accept", "application/json")
            }
            parameter("product_id", productId)
        }
        val bodyText = response.body<String>()
        return extractJsonFromResponse(bodyText)
    }
}
