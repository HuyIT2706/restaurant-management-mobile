package com.example.onefood.main.home.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.example.onefood.data.model.CartItem

@HiltViewModel
class CartViewModel @Inject constructor() : ViewModel() {
    
    // Map<TableId, List<CartItem>> - Giỏ hàng theo từng bàn
    private val _cartItems = MutableStateFlow<Map<Int, List<CartItem>>>(emptyMap())
    val cartItems: StateFlow<Map<Int, List<CartItem>>> = _cartItems.asStateFlow()
    
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
        
        // Update the table's items in the new map
        newCart[tableId] = tableItems.toList() // Create new immutable list
        
        // Update StateFlow with new map (this will trigger recomposition)
        _cartItems.value = newCart.toMap()
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
            
            // Create new map with new list
            val newCart = currentCart.toMutableMap()
            newCart[tableId] = updatedItems.toList()
            _cartItems.value = newCart.toMap()
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
        
        // Create new map
        val newCart = currentCart.toMutableMap()
        if (updatedItems.isEmpty()) {
            newCart.remove(tableId)
        } else {
            newCart[tableId] = updatedItems
        }
        
        _cartItems.value = newCart.toMap()
    }
    
    // Xóa toàn bộ giỏ hàng của một bàn
    fun clearCartForTable(tableId: Int) {
        val currentCart = _cartItems.value
        val newCart = currentCart.toMutableMap()
        newCart.remove(tableId)
        _cartItems.value = newCart.toMap()
    }
    
    // Xóa toàn bộ giỏ hàng
    fun clearAllCarts() {
        _cartItems.value = emptyMap()
    }
    
    // Kiểm tra bàn có giỏ hàng không
    fun hasCartForTable(tableId: Int): Boolean {
        return getCartItemsForTable(tableId).isNotEmpty()
    }
}

