package com.example.onefood.data.api

import com.example.onefood.data.model.CreateOrderRequest
import com.example.onefood.data.model.CreateOrderResponse
import com.example.onefood.data.model.OrderDetail
import com.example.onefood.data.model.OrderDetailResponse
import com.example.onefood.data.model.OrderListItem
import com.example.onefood.data.model.OrderListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.json.*

class OrderApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    // Helper function to extract JSON from response (handles BOM, whitespace, and extra text)
    private fun extractJsonFromResponse(response: String): String {
        val trimmed = response.trim()
        // Try to find JSON object boundaries
        val startIndex = trimmed.indexOfFirst { it == '{' }
        val lastIndex = trimmed.lastIndexOf('}')
        
        return if (startIndex >= 0 && lastIndex > startIndex) {
            trimmed.substring(startIndex, lastIndex + 1)
        } else {
            trimmed
        }
    }
    
    suspend fun createVnPayUrl(orderId: Int, token: String, bankCode: String = "NCB"): String {
        try {
            val responseText = try {
                val response = client.post("$baseUrl/payments/api_vnpay_create.php") {
                    headers {
                        append("Authorization", "Bearer $token")
                        append("Accept", "application/json")
                    }
                    contentType(ContentType.Application.Json)
                    val body = buildJsonObject {
                        put("order_id", orderId)
                        if (bankCode.isNotBlank()) {
                            put("bank_code", bankCode)
                        }
                    }
                    setBody(TextContent(body.toString(), ContentType.Application.Json))
                }
                val statusCode = response.status.value
                val bodyText = response.body<String>()
                if (statusCode >= 400) {
                    val jsonString = extractJsonFromResponse(bodyText)
                    val json = Json.parseToJsonElement(jsonString).jsonObject
                    val message = json["message"]?.jsonPrimitive?.content
                    throw Exception(
                        when (statusCode) {
                            403 -> message ?: "Bạn không có quyền khởi tạo thanh toán."
                            401 -> message ?: "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                            400 -> message ?: "Thiếu thông tin đơn hàng."
                            500 -> message ?: "Lỗi hệ thống. Vui lòng thử lại sau."
                            else -> message ?: "Lỗi kết nối: HTTP $statusCode"
                        }
                    )
                }
                bodyText
            } catch (e: Exception) {
                throw e
            }

            val jsonString = extractJsonFromResponse(responseText)
            val json = Json.parseToJsonElement(jsonString).jsonObject
            val success = json["success"]?.jsonPrimitive?.booleanOrNull ?: false
            if (!success) {
                val message = json["message"]?.jsonPrimitive?.content ?: "Không thể tạo thanh toán VNPay."
                throw Exception(message)
            }
            val url = json["vnpay_url"]?.jsonPrimitive?.content
            if (url.isNullOrBlank()) throw Exception("Không nhận được URL VNPay từ server.")
            return url
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> "Không thể kết nối đến server."
                e.message?.contains("Connection refused") == true -> "Server từ chối kết nối."
                else -> e.message ?: "Lỗi tạo thanh toán VNPay."
            }
            throw Exception(errorMessage, e)
        }
    }

    suspend fun payOrderWithCash(orderId: Int, token: String): String {
        try {
            val responseText = try {
                val response = client.post("$baseUrl/payments/api_payment_cash.php") {
                    headers {
                        append("Authorization", "Bearer $token")
                        append("Accept", "application/json")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(TextContent("""{"order_id": $orderId}""", ContentType.Application.Json))
                }
                val statusCode = response.status.value
                val bodyText = response.body<String>()
                if (statusCode >= 400) {
                    val jsonString = extractJsonFromResponse(bodyText)
                    val json = Json.parseToJsonElement(jsonString).jsonObject
                    val message = json["message"]?.jsonPrimitive?.content
                    throw Exception(
                        when (statusCode) {
                            403 -> message ?: "Bạn không có quyền thực hiện chức năng thanh toán."
                            401 -> message ?: "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                            400 -> message ?: "Thiếu thông tin đơn hàng."
                            500 -> message ?: "Lỗi hệ thống. Vui lòng thử lại sau."
                            else -> message ?: "Lỗi kết nối: HTTP $statusCode"
                        }
                    )
                }
                bodyText
            } catch (e: Exception) {
                throw e
            }

            val jsonString = extractJsonFromResponse(responseText)
            val json = Json.parseToJsonElement(jsonString).jsonObject
            val success = json["success"]?.jsonPrimitive?.booleanOrNull ?: false
            if (!success) {
                val message = json["message"]?.jsonPrimitive?.content ?: "Thanh toán tiền mặt thất bại."
                throw Exception(message)
            }
            return json["message"]?.jsonPrimitive?.content ?: "Thanh toán tiền mặt thành công."
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> "Không thể kết nối đến server."
                e.message?.contains("Connection refused") == true -> "Server từ chối kết nối."
                else -> e.message ?: "Không thể thanh toán bằng tiền mặt."
            }
            throw Exception(errorMessage, e)
        }
    }
    
    suspend fun createOrder(request: CreateOrderRequest, token: String): CreateOrderResponse {
        try {
            // Build JSON string manually using buildJsonObject
            val jsonBody = buildJsonObject {
                put("table_id", request.tableId)
                putJsonArray("items") {
                    request.items.forEach { item ->
                        addJsonObject {
                            put("product_id", item.productId)
                            put("quantity", item.quantity)
                            put("price", item.price)
                            put("notes", item.notes ?: "") // Ensure notes is never null
                        }
                    }
                }
            }.toString()
            
            // POST request with JSON body
            // Try to get response - Ktor may or may not throw exception for error status codes
            val responseText = try {
                val response = client.post("$baseUrl/carts/create_order.php") {
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(TextContent(text = jsonBody, contentType = ContentType.Application.Json))
                }
                // Check status code before reading body
                val statusCode = response.status.value
                val bodyText = response.body<String>()
                
                // If status code indicates error, try to parse error message from JSON
                if (statusCode >= 400) {
                    val jsonString = extractJsonFromResponse(bodyText)
                    val json = Json.parseToJsonElement(jsonString).jsonObject
                    val message = json["message"]?.jsonPrimitive?.content
                    
                    throw Exception(when (statusCode) {
                        403 -> message ?: "Bạn không có quyền thực hiện chức năng đặt đơn."
                        400 -> message ?: "Thiếu thông tin cần thiết. Vui lòng kiểm tra lại."
                        401 -> message ?: "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                        500 -> message ?: "Lỗi hệ thống. Vui lòng thử lại sau."
                        else -> message ?: "Lỗi kết nối: HTTP $statusCode"
                    })
                }
                
                bodyText
            } catch (e: Exception) {
                // If exception is already our custom exception, rethrow it
                if (e.message?.contains("Bạn không có quyền") == true ||
                    e.message?.contains("Thiếu thông tin") == true ||
                    e.message?.contains("Không có quyền") == true ||
                    e.message?.contains("Lỗi hệ thống") == true) {
                    throw e
                }
                
                // For other exceptions, check if it's a network error
                // If it contains response body info, try to parse it
                val errorMsg = e.message ?: ""
                if (errorMsg.contains("403") || errorMsg.contains("400") || errorMsg.contains("401")) {
                    throw Exception(when {
                        errorMsg.contains("403") -> "Bạn không có quyền thực hiện chức năng đặt đơn."
                        errorMsg.contains("400") -> "Thiếu thông tin cần thiết. Vui lòng kiểm tra lại."
                        errorMsg.contains("401") -> "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                        else -> "Lỗi kết nối: ${e.localizedMessage ?: e.message}"
                    })
                }
                
                // Re-throw network or other errors
                throw e
            }
            
            // Extract and parse JSON response
            val jsonString = extractJsonFromResponse(responseText)
            val json = Json.parseToJsonElement(jsonString).jsonObject
            
            val success = json["success"]?.jsonPrimitive?.content?.toBoolean()
            val message = json["message"]?.jsonPrimitive?.content
            
            // Check success flag in JSON response
            if (success == false) {
                throw Exception(message ?: "Lỗi khi đặt hàng")
            }
            
            // Success - return response
            return CreateOrderResponse(
                success = success,
                message = message,
                orderId = json["order_id"]?.jsonPrimitive?.content?.toIntOrNull(),
                totalAmount = json["total_amount"]?.jsonPrimitive?.content?.toDoubleOrNull()
            )
        } catch (e: Exception) {
            // Enhanced error handling
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> {
                    "Không thể kết nối đến server. Server đã chạy chưa?"
                }
                e.message?.contains("Connection refused") == true -> {
                    "Server từ chối kết nối. Địa chỉ IP có đúng không?"
                }
                e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true -> {
                    "Kết nối quá thời gian. Vui lòng thử lại."
                }
                e.message?.contains("401") == true -> {
                    "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                }
                e.message?.contains("403") == true -> {
                    "Bạn không có quyền thực hiện chức năng đặt đơn."
                }
                e.message?.contains("Failed to parse JSON") == true || e.message?.contains("Unexpected JSON token") == true -> {
                    "Lỗi định dạng dữ liệu từ server: ${e.message}"
                }
                else -> {
                    e.message ?: "Lỗi kết nối: ${e.localizedMessage ?: "Không xác định"}"
                }
            }
            throw Exception(errorMessage, e)
        }
    }
    
    suspend fun getOrderList(token: String): List<OrderListItem> {
        try {
            // Try to get response - Ktor may or may not throw exception for error status codes
            val responseText = try {
                val response = client.get("$baseUrl/orders/api_order_list.php") {
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }
                // Check status code before reading body
                val statusCode = response.status.value
                val bodyText = response.body<String>()
                
                // If status code indicates error, try to parse error message from JSON
                if (statusCode >= 400) {
                    val jsonString = extractJsonFromResponse(bodyText)
                    val json = Json.parseToJsonElement(jsonString).jsonObject
                    val message = json["message"]?.jsonPrimitive?.content
                    
                    throw Exception(when (statusCode) {
                        403 -> message ?: "Bạn không có quyền xem danh sách đơn hàng."
                        401 -> message ?: "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                        500 -> message ?: "Lỗi hệ thống. Vui lòng thử lại sau."
                        else -> message ?: "Lỗi kết nối: HTTP $statusCode"
                    })
                }
                
                bodyText
            } catch (e: Exception) {
                // If exception is already our custom exception, rethrow it
                if (e.message?.contains("Bạn không có quyền") == true ||
                    e.message?.contains("Không có quyền") == true ||
                    e.message?.contains("Lỗi hệ thống") == true ||
                    e.message?.contains("Lỗi kết nối: HTTP") == true) {
                    throw e
                }
                
                // For other exceptions, check if it's a network error
                val errorMsg = e.message ?: ""
                if (errorMsg.contains("403") || errorMsg.contains("401")) {
                    throw Exception(when {
                        errorMsg.contains("403") -> "Bạn không có quyền xem danh sách đơn hàng."
                        errorMsg.contains("401") -> "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                        else -> "Lỗi kết nối: ${e.localizedMessage ?: e.message}"
                    })
                }
                
                // Re-throw network or other errors
                throw e
            }
            
            // Extract and parse JSON response
            val jsonString = extractJsonFromResponse(responseText)
            val json = Json.parseToJsonElement(jsonString).jsonObject
            
            val success = json["success"]?.jsonPrimitive?.content?.toBoolean()
            
            // Check success flag in JSON response
            if (success == false) {
                val message = json["message"]?.jsonPrimitive?.content
                throw Exception(message ?: "Lỗi khi lấy danh sách đơn hàng")
            }
            
            // Parse orders array
            val ordersArray = json["orders"]?.jsonArray
            return if (ordersArray != null) {
                ordersArray.mapNotNull { orderElement ->
                    try {
                        Json.decodeFromJsonElement<OrderListItem>(orderElement.jsonObject)
                    } catch (e: Exception) {
                        null // Skip invalid orders
                    }
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // Enhanced error handling
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> {
                    "Không thể kết nối đến server. Server đã chạy chưa?"
                }
                e.message?.contains("Connection refused") == true -> {
                    "Server từ chối kết nối. Địa chỉ IP có đúng không?"
                }
                e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true -> {
                    "Kết nối quá thời gian. Vui lòng thử lại."
                }
                e.message?.contains("401") == true -> {
                    "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                }
                e.message?.contains("403") == true -> {
                    "Bạn không có quyền xem danh sách đơn hàng."
                }
                e.message?.contains("Failed to parse JSON") == true || e.message?.contains("Unexpected JSON token") == true -> {
                    "Lỗi định dạng dữ liệu từ server: ${e.message}"
                }
                else -> {
                    e.message ?: "Lỗi kết nối: ${e.localizedMessage ?: "Không xác định"}"
                }
            }
            throw Exception(errorMessage, e)
        }
    }
    
    suspend fun getOrderDetail(orderId: String, token: String): OrderDetail {
        try {
            // Try to get response - Ktor may or may not throw exception for error status codes
            val responseText = try {
                val response = client.get("$baseUrl/orders/api_get_order_detail.php") {
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                    parameter("order_id", orderId)
                }
                // Check status code before reading body
                val statusCode = response.status.value
                val bodyText = response.body<String>()
                
                // If status code indicates error, try to parse error message from JSON
                if (statusCode >= 400) {
                    val jsonString = extractJsonFromResponse(bodyText)
                    val json = Json.parseToJsonElement(jsonString).jsonObject
                    val message = json["message"]?.jsonPrimitive?.content
                    
                    throw Exception(when (statusCode) {
                        403 -> message ?: "Bạn không có quyền xem chi tiết đơn hàng."
                        401 -> message ?: "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                        404 -> message ?: "Không tìm thấy đơn hàng."
                        400 -> message ?: "Thiếu ID đơn hàng."
                        500 -> message ?: "Lỗi hệ thống. Vui lòng thử lại sau."
                        else -> message ?: "Lỗi kết nối: HTTP $statusCode"
                    })
                }
                
                bodyText
            } catch (e: Exception) {
                // If exception is already our custom exception, rethrow it
                if (e.message?.contains("Bạn không có quyền") == true ||
                    e.message?.contains("Không có quyền") == true ||
                    e.message?.contains("Không tìm thấy") == true ||
                    e.message?.contains("Thiếu ID") == true ||
                    e.message?.contains("Lỗi hệ thống") == true ||
                    e.message?.contains("Lỗi kết nối: HTTP") == true) {
                    throw e
                }
                
                // For other exceptions, check if it's a network error
                val errorMsg = e.message ?: ""
                if (errorMsg.contains("403") || errorMsg.contains("401") || errorMsg.contains("404") || errorMsg.contains("400")) {
                    throw Exception(when {
                        errorMsg.contains("403") -> "Bạn không có quyền xem chi tiết đơn hàng."
                        errorMsg.contains("401") -> "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                        errorMsg.contains("404") -> "Không tìm thấy đơn hàng."
                        errorMsg.contains("400") -> "Thiếu ID đơn hàng."
                        else -> "Lỗi kết nối: ${e.localizedMessage ?: e.message}"
                    })
                }
                
                // Re-throw network or other errors
                throw e
            }
            
            // Extract and parse JSON response
            val jsonString = extractJsonFromResponse(responseText)
            val json = Json.parseToJsonElement(jsonString).jsonObject
            
            val success = json["success"]?.jsonPrimitive?.content?.toBoolean()
            
            // Check success flag in JSON response
            if (success == false) {
                val message = json["message"]?.jsonPrimitive?.content
                throw Exception(message ?: "Lỗi khi lấy chi tiết đơn hàng")
            }
            
            // Parse data object and convert Int values to String for order_id and table_id
            val dataJson = json["data"]?.jsonObject
            if (dataJson != null) {
                // Helper function to convert JsonElement to String
                fun JsonElement?.toStringValue(): String? {
                    return when (this) {
                        is JsonPrimitive -> {
                            if (this.isString) {
                                this.content
                            } else {
                                // It's a number, convert to string
                                this.content
                            }
                        }
                        null -> null
                        else -> this.toString().trim('"')
                    }
                }
                
                // Create a new JSON object with converted values
                val convertedData = buildJsonObject {
                    dataJson.forEach { (key, value) ->
                        when (key) {
                            "order_id", "table_id" -> {
                                // Convert Int to String if needed
                                val stringValue = value.toStringValue()
                                if (stringValue != null) {
                                    put(key, stringValue)
                                } else {
                                    put(key, value)
                                }
                            }
                            "items" -> {
                                // Handle items array - convert quantity and price if needed
                                if (value is JsonArray) {
                                    putJsonArray(key) {
                                        value.forEach { itemElement ->
                                            if (itemElement is JsonObject) {
                                                addJsonObject {
                                                    itemElement.forEach { (itemKey, itemValue) ->
                                                        when (itemKey) {
                                                            "order_detail_quantity", "order_detail_price" -> {
                                                                val itemStringValue = itemValue.toStringValue()
                                                                if (itemStringValue != null) {
                                                                    put(itemKey, itemStringValue)
                                                                } else {
                                                                    put(itemKey, itemValue)
                                                                }
                                                            }
                                                            else -> put(itemKey, itemValue)
                                                        }
                                                    }
                                                }
                                            } else {
                                                add(itemElement)
                                            }
                                        }
                                    }
                                } else {
                                    put(key, value)
                                }
                            }
                            else -> put(key, value)
                        }
                    }
                }
                
                // Use Json with lenient mode to handle the conversion
                val lenientJson = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                }
                
                return lenientJson.decodeFromJsonElement<OrderDetail>(convertedData)
            } else {
                throw Exception("Dữ liệu đơn hàng không hợp lệ")
            }
        } catch (e: Exception) {
            // Enhanced error handling
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> {
                    "Không thể kết nối đến server. Server đã chạy chưa?"
                }
                e.message?.contains("Connection refused") == true -> {
                    "Server từ chối kết nối. Địa chỉ IP có đúng không?"
                }
                e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true -> {
                    "Kết nối quá thời gian. Vui lòng thử lại."
                }
                e.message?.contains("401") == true -> {
                    "Không có quyền thực hiện thao tác này. Vui lòng đăng nhập lại."
                }
                e.message?.contains("403") == true -> {
                    "Bạn không có quyền xem chi tiết đơn hàng."
                }
                e.message?.contains("404") == true -> {
                    "Không tìm thấy đơn hàng."
                }
                e.message?.contains("Failed to parse JSON") == true || 
                e.message?.contains("Unexpected JSON token") == true ||
                e.message?.contains("String literal") == true ||
                e.message?.contains("should be quoted") == true -> {
                    "Lỗi định dạng dữ liệu từ server. Vui lòng thử lại sau."
                }
                else -> {
                    e.message ?: "Lỗi kết nối: ${e.localizedMessage ?: "Không xác định"}"
                }
            }
            throw Exception(errorMessage, e)
        }
    }
}

