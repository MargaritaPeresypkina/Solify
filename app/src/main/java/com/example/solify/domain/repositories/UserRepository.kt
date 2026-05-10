package com.example.solify.domain.repositories

import com.example.solify.domain.entities.user.User
import kotlinx.coroutines.flow.Flow
import java.io.File

interface UserRepository {

    suspend fun getUserById(userId: String): Result<User>

    fun observeUserById(userId: String): Flow<User?>

    suspend fun updateUser(userId: String, name: String, surname: String, email: String): Result<User>

    suspend fun isEmailExists(email: String): Result<Boolean>

    suspend fun registerUser(email: String, password: String, name: String, surname: String): Result<User>

    suspend fun loginUser(email: String, password: String): Result<User>

    suspend fun logoutUser(): Result<Unit>

    suspend fun updateUserAvatar(userId: String, avatarFile: File): Result<String>

    suspend fun deleteUserAvatar(userId: String): Result<Unit>

    suspend fun getUserByEmail(email: String): Result<User?>

    suspend fun deleteUser(userId: String, password: String): Result<Boolean>
}