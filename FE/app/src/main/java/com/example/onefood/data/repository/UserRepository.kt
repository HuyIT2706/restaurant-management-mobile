package com.example.onefood.data.repository

import com.example.onefood.data.api.UserApiService
import com.example.onefood.data.model.*

class UserRepository(private val userApiService: UserApiService) {

    /**
     * Lấy danh sách nhân viên từ API
     */
    suspend fun getUsers(token: String): Result<List<User>> {
        return try {
            val response = userApiService.getUsers(token)
            if (response.status == "success" && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không tìm thấy nhân viên nào"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Thêm nhân viên mới
     */
    suspend fun addUser(
        fullname: String,
        phone: String,
        gender: String?,
        role: String,
        wage: Double?,
        status: Int,
        image: String?,
        token: String
    ): Result<Int> {
        return try {
            val response = userApiService.addUser(
                fullname = fullname,
                phone = phone,
                gender = gender,
                role = role,
                wage = wage,
                status = status,
                image = image,
                token = token
            )
            if (response.status == "success" && response.userId != null) {
                Result.success(response.userId)
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi thêm nhân viên"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật thông tin nhân viên
     */
    suspend fun updateUser(
        userId: Int,
        fullname: String,
        phone: String,
        gender: String?,
        role: String,
        wage: Double?,
        status: Int,
        image: String?,
        token: String
    ): Result<Unit> {
        return try {
            val response = userApiService.updateUser(
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
            if (response.status == "success") {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi cập nhật nhân viên"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xoá nhân viên
     */
    suspend fun deleteUser(userId: Int, token: String): Result<Unit> {
        return try {
            val response = userApiService.deleteUser(userId, token)
            if (response.status == "success") {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi xoá nhân viên"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}