package com.example.solify.data.repositories

import android.util.Log
import com.example.solify.data.local.data_sources.UserLocalDataSource
import com.example.solify.data.remote.firebase.auth_service.FirebaseAuthService
import com.example.solify.data.remote.firebase.data_source.UserRemoteDataSource
import com.example.solify.domain.entities.user.User
import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.utils.hashPassword
import com.example.solify.domain.utils.verifyPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localDataSource: UserLocalDataSource,
    private val remoteAuthService: FirebaseAuthService,
    private val userRemoteDataSource: UserRemoteDataSource
) : UserRepository {

    override fun observeUserById(userId: String): Flow<User?> {
        return localDataSource.observeUserById(userId)
    }

    override suspend fun getUserById(userId: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val cachedUser = runCatching { localDataSource.getUserById(userId) }.getOrNull()

                if (cachedUser != null) {
                    try {
                        userRemoteDataSource.getUserProfile(userId).onSuccess { remoteUser ->
                            if (remoteUser != cachedUser) {
                                localDataSource.updateUser(remoteUser)
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("UserRepo", "Background update failed: ${e.message}")
                    }
                    return@withContext Result.success(cachedUser)
                }

                val remoteUser = userRemoteDataSource.getUserProfile(userId).getOrNull()

                if (remoteUser != null) {
                    localDataSource.insertUser(remoteUser)
                    Result.success(remoteUser)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get user: ${e.message}", e))
            }
        }
    }

    override suspend fun updateUser(
        userId: String,
        name: String,
        surname: String,
        email: String,
        password: String?
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = localDataSource.getUserById(userId)
                val normalizedEmail = email.trim().lowercase()
                val currentEmail = currentUser.email.lowercase()

                // Если email изменился
                if (normalizedEmail != currentEmail) {
                    if (password.isNullOrBlank()) {
                        return@withContext Result.failure(
                            IllegalArgumentException("Password required to change email")
                        )
                    }

                    // reauthentication
                    val reauthResult = remoteAuthService.reauthenticateUser(password)
                    if (reauthResult.isFailure) {
                        return@withContext Result.failure(
                            Exception("Invalid password. Please try again.")
                        )
                    }

                    // обновление email в Auth
                    val authResult = remoteAuthService.updateUserEmail(normalizedEmail)
                    if (authResult.isFailure) {
                        return@withContext Result.failure(
                            Exception("Failed to update email: ${authResult.exceptionOrNull()?.message}")
                        )
                    }
                }

                val updatedUser = User(
                    id = userId,
                    name = name,
                    surname = surname,
                    email = normalizedEmail,
                    avatarUrl = currentUser.avatarUrl,
                    passwordHash = currentUser.passwordHash
                )

                userRemoteDataSource.updateUserProfile(updatedUser).getOrThrow()
                localDataSource.updateUser(updatedUser)

                Result.success(updatedUser)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update profile: ${e.message}", e))
            }
        }
    }

    override suspend fun updateUserAvatar(userId: String, avatarFile: File): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val avatarUrl = userRemoteDataSource.uploadAvatar(userId, avatarFile).getOrThrow()
                Log.d("ava", "avatarUrl $avatarUrl")
                val currentUser = getUserById(userId).getOrNull()
                Log.d("ava", "currentUser $currentUser")

                if (currentUser != null) {
                    val updatedUser = currentUser.copy(avatarUrl = avatarUrl)
                    Log.d("ava", "updatedUser $updatedUser")
                    localDataSource.updateUser(updatedUser)
                }

                Result.success(avatarUrl)
            } catch (e: Exception) {
                Result.failure(Exception("Avatar upload failed: ${e.message}"))
            }
        }
    }

    override suspend fun deleteUserAvatar(userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                userRemoteDataSource.deleteAvatar(userId).getOrThrow()

                val currentUser = getUserById(userId).getOrNull()
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(avatarUrl = null)
                    localDataSource.updateUser(updatedUser)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Avatar deletion failed: ${e.message}"))
            }
        }
    }

    override suspend fun isEmailExists(email: String, excludeUserId: String?): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(userRemoteDataSource.isEmailExists(email,excludeUserId))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to check email: ${e.message}", e))
            }
        }
    }

    override suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        surname: String
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = remoteAuthService.registerUser(email, password)

                authResult.onSuccess { firebaseUser ->
                    val user = User(
                        id = firebaseUser.uid,
                        name = name,
                        surname = surname,
                        email = email,
                        passwordHash = hashPassword(password),
                        avatarUrl = null
                    )

                    remoteAuthService.saveUserProfile(user).getOrThrow()

                    localDataSource.insertUser(user)

                    return@withContext Result.success(user)
                }

                authResult.onFailure { error ->
                    return@withContext Result.failure(error)
                }

                Result.failure(Exception("Registration failed"))
            } catch (e: Exception) {
                Result.failure(Exception("Registration failed: ${e.message}"))
            }
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = remoteAuthService.loginUser(email, password)
                Log.d("test", "authResult $authResult")
                authResult.onSuccess { firebaseUser ->
                    val user = getUserById(firebaseUser.uid).getOrNull()
                    Log.d("test", "authResult user $user")

                    if (user != null) {
                        if (!verifyPassword(password, user.passwordHash)) {
                            return@withContext Result.failure(IllegalArgumentException("Invalid password"))
                        }
                        return@withContext Result.success(user)
                    } else {
                        return@withContext Result.failure(Exception("User profile not found"))
                    }
                }.onFailure { error ->
                    return@withContext Result.failure(error)
                }

                Result.failure(Exception("Login failed"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun logoutUser(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                remoteAuthService.logoutUser()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Logout failed: ${e.message}"))
            }
        }
    }

    override suspend fun getUserByEmail(email: String): Result<User?> {
        return withContext(Dispatchers.IO) {
            try {
                val cachedUser = runCatching { localDataSource.getUserByEmail(email) }.getOrNull()

                if (cachedUser != null) {
                    return@withContext Result.success(cachedUser)
                }

                Result.success(null)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get user: ${e.message}", e))
            }
        }
    }

    override suspend fun deleteUser(userId: String, password: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val user = getUserById(userId).getOrNull()
                    ?: return@withContext Result.failure(IllegalStateException("User not found"))

                if (!verifyPassword(password, user.passwordHash)) {
                    return@withContext Result.success(false)
                }
                remoteAuthService.reauthenticateAndDeleteUser(password).getOrThrow()
                localDataSource.deleteUser(userId)

                Result.success(true)
            } catch (e: Exception) {
                Result.failure(Exception("Account deletion failed: ${e.message}"))
            }
        }
    }
}
