package com.example.solify.data.repositories

import com.example.solify.data.dao.UserDao
import com.example.solify.data.mappers.toDbModel
import com.example.solify.data.mappers.toDomain
import com.example.solify.domain.entities.user.User
import com.example.solify.domain.repositories.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val userDb = userDao.getUserById(userId).firstOrNull()
            Result.success(userDb?.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get user: ${e.message}"))
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userDao.updateUser(user.toDbModel())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update user: ${e.message}"))
        }
    }


    override suspend fun isEmailExists(email: String): Result<Boolean> {
        return try {
            val user = userDao.getUserByEmail(email)
            Result.success(user != null)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to check email: ${e.message}"))
        }
    }

    override suspend fun registerUser(user: User): Result<Unit> {
        return try {
            userDao.insertUser(user.toDbModel())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save user: ${e.message}"))
        }
    }

    override suspend fun getUserByEmail(email: String): Result<User?> {
        return try {
            val userDb = userDao.getUserByEmail(email)
            Result.success(userDb?.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get user: ${e.message}"))
        }
    }

    override fun observeUserById(userId: String): Flow<User?> {
        return userDao.getUserById(userId).map { it.toDomain() }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            userDao.deleteUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete user: ${e.message}"))
        }
    }
}