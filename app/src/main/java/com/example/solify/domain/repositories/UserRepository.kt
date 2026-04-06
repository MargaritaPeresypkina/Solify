package com.example.solify.domain.repositories

import com.example.solify.domain.entities.user.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun getUserById(userId: String): Result<User?>

    suspend fun updateUser(user: User): Result<Unit>

    suspend fun isEmailExists(email: String): Result<Boolean>

    suspend fun registerUser(user: User): Result<Unit>

    suspend fun getUserByEmail(email: String): Result<User?>

    fun observeUserById(userId: String): Flow<User?>

    suspend fun deleteUser(userId: String): Result<Unit>
}