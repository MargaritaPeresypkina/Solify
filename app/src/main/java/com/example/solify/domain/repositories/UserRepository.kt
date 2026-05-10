package com.example.solify.domain.repositories

import com.example.solify.domain.entities.user.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun getUserById(userId: String): Result<User?>

    fun observeUserById(userId: String): Flow<User?>

    suspend fun updateUser(userId: String, name: String, surname: String, email: String): Result<User>

    suspend fun isEmailExists(email: String): Result<Boolean>

    suspend fun registerUser(user: User): Result<Unit>

    suspend fun getUserByEmail(email: String): Result<User?>

<<<<<<< Updated upstream
    fun observeUserById(userId: String): Flow<User?>

    suspend fun deleteUser(userId: String): Result<Unit>
=======
    suspend fun deleteUser(userId: String, password: String): Result<Boolean>
>>>>>>> Stashed changes
}