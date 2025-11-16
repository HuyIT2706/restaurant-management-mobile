package com.example.onefood.main.home.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.api.TableApiService
import com.example.onefood.data.model.SelectTableRequest
import com.example.onefood.data.model.SelectTableResponse
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
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
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
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
            } catch (e: HttpException) {
                val errorMessage = try {
                    val errorBody = e.response()?.errorBody()?.string()
                    if (!errorBody.isNullOrEmpty()) {
                        try {
                            val errorResponseAdapter = moshi.adapter(SelectTableResponse::class.java)
                            val errorResponse = errorResponseAdapter.fromJson(errorBody)
                            errorResponse?.message ?: "Lỗi: HTTP ${e.code()}"
                        } catch (parseException: Exception) {
                            val messageMatch = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(errorBody)
                            messageMatch?.groupValues?.get(1) ?: "Lỗi: HTTP ${e.code()}"
                        }
                    } else {
                        "Lỗi: HTTP ${e.code()}"
                    }
                } catch (ex: Exception) {
                    "Lỗi: HTTP ${e.code()}"
                }
                _state.value = SelectTableState.Error(errorMessage)
            } catch (e: IOException) {
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true -> 
                        "Không thể kết nối đến server. Kiểm tra kết nối mạng!"
                    e.message?.contains("Connection refused") == true -> 
                        "Server từ chối kết nối. Server đã chạy chưa?"
                    e.message?.contains("timeout") == true -> 
                        "Kết nối quá thời gian. Vui lòng thử lại!"
                    else -> "Lỗi kết nối: ${e.message ?: "Không xác định"}"
                }
                _state.value = SelectTableState.Error(errorMessage)
            } catch (e: Exception) {
                _state.value = SelectTableState.Error(
                    e.message ?: "Đã xảy ra lỗi không xác định"
                )
            }
        }
    }

    fun resetState() {
        _state.value = SelectTableState.Initial
    }
}

