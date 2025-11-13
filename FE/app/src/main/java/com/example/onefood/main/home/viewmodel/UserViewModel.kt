package com.example.onefood.main.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onefood.data.model.User
import com.example.onefood.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class UserUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val successMessage: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    /**
     * Lấy danh sách nhân viên từ API
     */
    fun loadUsers(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = userRepository.getUsers(token)
            result.onSuccess { users ->
                _uiState.value = _uiState.value.copy(
                    users = users,
                    isLoading = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Lỗi không xác định"
                )
            }
        }
    }

    /**
     * Thêm nhân viên mới
     */
    fun addUser(
        fullname: String,
        phone: String,
        gender: String?,
        role: String,
        wage: Double?,
        status: Int,
        image: String?,
        token: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = userRepository.addUser(
                fullname = fullname,
                phone = phone,
                gender = gender,
                role = role,
                wage = wage,
                status = status,
                image = image,
                token = token
            )
            result.onSuccess { userId ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Thêm nhân viên thành công!"
                )
                // Reload danh sách
                loadUsers(token)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Lỗi khi thêm nhân viên"
                )
            }
        }
    }

    /**
     * Cập nhật nhân viên
     */
    fun updateUser(
        userId: Int,
        fullname: String,
        phone: String,
        gender: String?,
        role: String,
        wage: Double?,
        status: Int,
        image: String?,
        token: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = userRepository.updateUser(
                userId = userId,
                fullname = fullname,
                phone = phone,
                gender = gender,
                role = role,
                wage = wage,
                status = status,
                image = image,
                token = token
            )
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Cập nhật nhân viên thành công!"
                )
                // Reload danh sách
                loadUsers(token)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Lỗi khi cập nhật nhân viên"
                )
            }
        }
    }

    /**
     * Xoá nhân viên
     */
    fun deleteUser(userId: Int, token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = userRepository.deleteUser(userId, token)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Xoá nhân viên thành công!"
                )
                // Reload danh sách
                loadUsers(token)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Lỗi khi xoá nhân viên"
                )
            }
        }
    }

    /**
     * Cập nhật query tìm kiếm
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /**
     * Xóa thông báo lỗi/thành công
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}