package com.example.onefood.main.home.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.example.onefood.data.model.CartItem

@HiltViewModel
class CartViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) : ViewModel() {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("onefood_prefs", Context.MODE_PRIVATE)
    private val CART_KEY = "cart_items"
    
    private val cartMapType = Types.newParameterizedType(
        Map::class.java,
        Int::class.javaObjectType,
        Types.newParameterizedType(
            List::class.java,
            CartItem::class.java
        )
    )
    private val cartAdapter = moshi.adapter<Map<Int, List<CartItem>>>(cartMapType)
    
    private val _cartItems = MutableStateFlow<Map<Int, List<CartItem>>>(loadCartFromPrefs())
    val cartItems: StateFlow<Map<Int, List<CartItem>>> = _cartItems.asStateFlow()
    
    private fun loadCartFromPrefs(): Map<Int, List<CartItem>> {
        val cartJson = prefs.getString(CART_KEY, null) ?: return emptyMap()
        return try {
            cartAdapter.fromJson(cartJson) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    private fun saveCartToPrefs(cart: Map<Int, List<CartItem>>) {
        try {
            val cartJson = cartAdapter.toJson(cart)
            prefs.edit().putString(CART_KEY, cartJson).apply()
        } catch (e: Exception) {
        }
    }
    
    // Lấy giỏ hàng của một bàn cụ thể
    fun getCartItemsForTable(tableId: Int): List<CartItem> {
        return _cartItems.value[tableId] ?: emptyList()
    }
    
    // Lấy danh sách các bàn có giỏ hàng
    fun getTablesWithCart(): List<Int> {
        return _cartItems.value.keys.filter { tableId ->
            val items = _cartItems.value[tableId] ?: emptyList()
            items.isNotEmpty()
        }.sorted()
    }
    
    // Tính tổng tiền của một bàn
    fun getTotalPriceForTable(tableId: Int): Int {
        return getCartItemsForTable(tableId).sumOf { it.totalPrice }
    }
    
    // Thêm sản phẩm vào giỏ hàng của một bàn
    fun addItemToCart(tableId: Int, item: CartItem) {
        // Get current cart and create a deep copy to ensure StateFlow emits new value
        val currentCart = _cartItems.value
        val newCart = mutableMapOf<Int, List<CartItem>>()
        
        // Copy all existing tables
        currentCart.forEach { (existingTableId, existingItems) ->
            newCart[existingTableId] = existingItems.toList() // Create new list
        }
        
        // Get or create items for this table
        val tableItems = newCart[tableId]?.toMutableList() ?: mutableListOf()
        
        // Kiểm tra xem sản phẩm đã có trong giỏ chưa (cùng productId và notes)
        val existingIndex = tableItems.indexOfFirst { 
            it.productId == item.productId && it.notes == item.notes 
        }
        
        if (existingIndex != -1) {
            // Nếu đã có, tăng quantity
            val existingItem = tableItems[existingIndex]
            tableItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + item.quantity)
        } else {
            // Nếu chưa có, thêm mới
            tableItems.add(item)
        }
        
        newCart[tableId] = tableItems.toList()
        
        val finalCart = newCart.toMap()
        _cartItems.value = finalCart
        saveCartToPrefs(finalCart)
    }
    
    // Cập nhật quantity của một item
    fun updateItemQuantity(tableId: Int, itemId: Int, notes: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItemFromCart(tableId, itemId, notes)
            return
        }
        
        val currentCart = _cartItems.value
        val tableItems = currentCart[tableId]?.toMutableList() ?: return
        
        val index = tableItems.indexOfFirst { 
            it.productId == itemId && it.notes == notes 
        }
        
        if (index != -1) {
            // Create new list with updated item to ensure StateFlow detects change
            val updatedItems = tableItems.toMutableList()
            updatedItems[index] = updatedItems[index].copy(quantity = newQuantity)
            
            val newCart = currentCart.toMutableMap()
            newCart[tableId] = updatedItems.toList()
            val finalCart = newCart.toMap()
            _cartItems.value = finalCart
            saveCartToPrefs(finalCart)
        }
    }
    
    // Xóa một item khỏi giỏ hàng
    fun removeItemFromCart(tableId: Int, itemId: Int, notes: String) {
        val currentCart = _cartItems.value
        val tableItems = currentCart[tableId]?.toMutableList() ?: return
        
        // Create new list without the item
        val updatedItems = tableItems.filterNot { 
            it.productId == itemId && it.notes == notes 
        }
        
        val newCart = currentCart.toMutableMap()
        if (updatedItems.isEmpty()) {
            newCart.remove(tableId)
        } else {
            newCart[tableId] = updatedItems
        }
        
        val finalCart = newCart.toMap()
        _cartItems.value = finalCart
        saveCartToPrefs(finalCart)
    }
    
    fun clearCartForTable(tableId: Int) {
        val currentCart = _cartItems.value
        val newCart = currentCart.toMutableMap()
        newCart.remove(tableId)
        val finalCart = newCart.toMap()
        _cartItems.value = finalCart
        saveCartToPrefs(finalCart)
    }
    
    fun clearAllCarts() {
        _cartItems.value = emptyMap()
        saveCartToPrefs(emptyMap())
    }
    
    // Kiểm tra bàn có giỏ hàng không
    fun hasCartForTable(tableId: Int): Boolean {
        return getCartItemsForTable(tableId).isNotEmpty()
    }
}

