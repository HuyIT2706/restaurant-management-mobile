package com.example.onefood.data.api

import com.example.onefood.data.model.Product
import com.example.onefood.data.model.UploadImageResponse
import com.example.onefood.data.model.AddProductResponse
import com.example.onefood.data.model.DeleteProductResponse
import com.example.onefood.data.model.UpdateProductResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.json.*

class ProductApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
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
    
    suspend fun getProducts(page: Int = 1, limit: Int = 50): List<Product> {
        val response: String = client.get("$baseUrl/products/product.php") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()
        
        // Try to parse as paginated response first
        return try {
            val json = Json.parseToJsonElement(response).jsonObject
            if (json.containsKey("data") && json.containsKey("pagination")) {
                // New paginated format
                json["data"]?.jsonArray?.mapNotNull { 
                    Json.decodeFromJsonElement<Product>(it)
                } ?: emptyList()
            } else {
                // Old format (backward compatible)
                Json.decodeFromString<List<Product>>(response)
            }
        } catch (e: Exception) {
            // Fallback to old format if parsing fails
            Json.decodeFromString<List<Product>>(response)
        }
    }
    
    suspend fun uploadImage(imageBytes: ByteArray, fileName: String, token: String): UploadImageResponse {
        // For multipart requests, we need to parse JSON response manually
        val responseText = client.post("$baseUrl/products/api_upimage.php") {
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(MultiPartFormDataContent(
                formData {
                    append("image", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"image\"; filename=\"$fileName\"")
                    })
                }
            ))
        }.body<String>()
        
        // Extract and parse JSON response manually for better error handling
        val jsonString = extractJsonFromResponse(responseText)
        return try {
            val json = Json.parseToJsonElement(jsonString).jsonObject
            UploadImageResponse(
                success = json["success"]?.jsonPrimitive?.content?.toBoolean() ?: false,
                message = json["message"]?.jsonPrimitive?.content ?: "",
                imageUrl = json["image_url"]?.jsonPrimitive?.content,
                fileName = json["file_name"]?.jsonPrimitive?.content
            )
        } catch (e: Exception) {
            // If JSON parsing fails, throw with response for debugging
            throw Exception("Failed to parse JSON response: ${e.message}. Response length: ${responseText.length}, JSON: $jsonString", e)
        }
    }
    
    suspend fun addProduct(
        name: String,
        description: String,
        category: String,
        price: Double,
        imageUrl: String,
        token: String
    ): AddProductResponse {
        // Build JSON string manually to ensure proper formatting
        val jsonBody = buildJsonObject {
            put("name", name)
            put("description", description)
            put("category", category)
            put("price", price)
            put("image_url", imageUrl)
        }.toString()
        
        // POST request with JSON body as TextContent (bypasses ContentNegotiation serialization)
        val responseText = client.post("$baseUrl/products/create_product.php") {
            headers {
                append("Authorization", "Bearer $token")
            }
            contentType(ContentType.Application.Json)
            setBody(TextContent(text = jsonBody, contentType = ContentType.Application.Json))
        }.body<String>()
        
        // Extract and parse JSON response
        val jsonString = extractJsonFromResponse(responseText)
        val json = Json.parseToJsonElement(jsonString).jsonObject
        
        return AddProductResponse(
            success = json["success"]?.jsonPrimitive?.content?.toBoolean(),
            message = json["message"]?.jsonPrimitive?.content,
            productId = json["product_id"]?.jsonPrimitive?.content?.toIntOrNull()
        )
    }
    
    suspend fun getProductById(productId: Int): Product? {
        val products = getProducts()
        return products.find { it.id == productId }
    }
    
    suspend fun updateProduct(
        productId: Int,
        name: String,
        description: String,
        category: String,
        price: Double,
        imageUrl: String,
        productActive: Int,
        token: String
    ): UpdateProductResponse {
        // Build JSON string manually
        val jsonBody = buildJsonObject {
            put("product_id", productId)
            put("name", name)
            put("description", description)
            put("category", category)
            put("price", price)
            put("image_url", imageUrl)
            put("product_active", productActive)
        }.toString()
        
        // PUT request with JSON body
        val responseText = client.put("$baseUrl/products/api_product_update.php") {
            headers {
                append("Authorization", "Bearer $token")
            }
            contentType(ContentType.Application.Json)
            setBody(TextContent(text = jsonBody, contentType = ContentType.Application.Json))
        }.body<String>()
        
        // Extract and parse JSON response
        val jsonString = extractJsonFromResponse(responseText)
        val json = Json.parseToJsonElement(jsonString).jsonObject
        
        return UpdateProductResponse(
            success = json["success"]?.jsonPrimitive?.content?.toBoolean(),
            message = json["message"]?.jsonPrimitive?.content
        )
    }
    
    suspend fun deleteProduct(productId: Int, token: String): DeleteProductResponse {
        // DELETE request with product ID as query parameter
        val responseText = client.delete("$baseUrl/products/api_product_delete.php?id=$productId") {
            headers {
                append("Authorization", "Bearer $token")
            }
        }.body<String>()
        
        // Extract and parse JSON response
        val jsonString = extractJsonFromResponse(responseText)
        val json = Json.parseToJsonElement(jsonString).jsonObject
        
        return DeleteProductResponse(
            success = json["success"]?.jsonPrimitive?.content?.toBoolean(),
            message = json["message"]?.jsonPrimitive?.content
        )
    }
}

