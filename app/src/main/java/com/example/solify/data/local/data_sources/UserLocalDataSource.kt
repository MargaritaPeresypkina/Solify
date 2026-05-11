package com.example.solify.data.local.data_sources

import android.util.Log
import com.example.solify.data.local.dao.UserDao
import com.example.solify.data.local.mappers.toDbModel
import com.example.solify.data.local.mappers.toDomain
import com.example.solify.domain.entities.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocalDataSource @Inject constructor(
    private val userDao: UserDao
) {

    fun observeUserById(userId: String): Flow<User?> {
        return userDao.getUserById(userId).map { userDb ->
            try {
                userDb?.toDomain()
            } catch (e: Exception) {
                null
            }
        }.catch { exception ->
            Log.e("UserLocalDataSource", "Error observing user", exception)
            emit(null)
        }
    }

    suspend fun getUserById(userId: String): User {
        return try {
            val userDb = userDao.getUserByIdSuspend(userId)
            userDb?.toDomain() ?: throw Exception("User not found")
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get user by id $userId", e)
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            val userDb = userDao.getUserByEmail(email)
            userDb?.toDomain()
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get user by email $email", e)
        }
    }

    suspend fun isEmailExists(email: String): Boolean {
        return try {
            userDao.getUserByEmail(email) != null
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to check email existence", e)
        }
    }

    suspend fun insertUser(user: User) {
        try {
            userDao.insertUser(user.toDbModel())
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to insert user", e)
        }
    }

    suspend fun updateUser(user: User) {
        try {
            userDao.updateUser(user.toDbModel())
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to update user", e)
        }
    }

    suspend fun deleteUser(userId: String) {
        try {
            userDao.deleteUser(userId)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to delete user $userId", e)
        }
    }
}