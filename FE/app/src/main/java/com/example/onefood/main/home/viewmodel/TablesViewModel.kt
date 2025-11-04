package com.example.onefood.main.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.model.Table
import com.example.onefood.data.repository.TableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TablesViewModel @Inject constructor(
    private val repository: TableRepository
) : ViewModel() {

    private val _tables = MutableStateFlow<List<Table>>(emptyList())
    val tables: StateFlow<List<Table>> = _tables

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadTables()
    }

    fun loadTables() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getTables()
                .catch { e ->
                    _error.value = "Error: ${e.message ?: e.javaClass.simpleName}"
                    _isLoading.value = false
                }
                .collect { tables ->
                    _tables.value = tables
                    _isLoading.value = false
                }
        }
    }
}