package com.example.onefood.main.home.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.api.TableApiService
import com.example.onefood.data.model.SelectTableRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SelectTableState {
    object Initial : SelectTableState()
    object Loading : SelectTableState()
    data class Success(val message: String, val newStatus: String?) : SelectTableState()
    data class Error(val message: String) : SelectTableState()
}

@HiltViewModel
class SelectTableViewModel @Inject constructor(
    private val apiService: TableApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<SelectTableState>(SelectTableState.Initial)
    val state: StateFlow<SelectTableState> = _state

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("OneFoodPrefs", Context.MODE_PRIVATE)

    fun selectTable(tableId: Int) {
        viewModelScope.launch {
            _state.value = SelectTableState.Loading
            try {
                val request = SelectTableRequest(tableId = tableId)
                val response = apiService.selectTable(request)
                
                if (response.success == true) {
                    _state.value = SelectTableState.Success(
                        message = response.message ?: "Bàn đã được chọn!",
                        newStatus = response.newStatus
                    )
                } else {
                    _state.value = SelectTableState.Error(
                        response.message ?: "Không thể chọn bàn!"
                    )
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true -> 
                        "Không thể kết nối đến server. Kiểm tra kết nối mạng!"
                    e.message?.contains("Connection refused") == true -> 
                        "Server từ chối kết nối. Server đã chạy chưa?"
                    e.message?.contains("timeout") == true -> 
                        "Kết nối quá thời gian. Vui lòng thử lại!"
                    else -> "Lỗi: ${e.message ?: e.javaClass.simpleName}"
                }
                _state.value = SelectTableState.Error(errorMessage)
            }
        }
    }

    fun resetState() {
        _state.value = SelectTableState.Initial
    }
}

