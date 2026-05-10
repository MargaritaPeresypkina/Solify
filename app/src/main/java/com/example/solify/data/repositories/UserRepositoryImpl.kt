package com.example.solify.data.repositories

<<<<<<< Updated upstream
import com.example.solify.data.dao.UserDao
import com.example.solify.data.mappers.toDbModel
import com.example.solify.data.mappers.toDomain
=======
import android.util.Log
import com.example.solify.data.local.data_sources.UserLocalDataSource
import com.example.solify.data.remote.firebase.auth_service.FirebaseAuthService
import com.example.solify.data.remote.firebase.data_source.UserRemoteDataSource
>>>>>>> Stashed changes
import com.example.solify.domain.entities.user.User
import com.example.solify.domain.repositories.UserRepository
import kotlinx.coroutines.flow.Flow
<<<<<<< Updated upstream
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
=======
import kotlinx.coroutines.withContext
import java.io.File
>>>>>>> Stashed changes
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

<<<<<<< Updated upstream
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
=======
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
                Result.failure(DomainException.DataError("Failed to get user: ${e.message}", e))
            }
        }
    }

    override suspend fun updateUser(
        userId: String,
        name: String,
        surname: String,
        email: String
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = localDataSource.getUserById(userId)

                if (email != currentUser.email) {
                    val emailExists = userRemoteDataSource.isEmailExists(email)
                    if (emailExists) {
                        return@withContext Result.failure(IllegalArgumentException("Email already in use"))
                    }
                }

                val updatedUser = User(
                    id = userId,
                    name = name,
                    surname = surname,
                    email = email,
                    avatarUrl = currentUser.avatarUrl,
                    passwordHash = currentUser.passwordHash
                )

                userRemoteDataSource.updateUserProfile(updatedUser).getOrThrow()
                localDataSource.updateUser(updatedUser)

                Result.success(updatedUser)
            } catch (e: Exception) {
                Result.failure(DomainException.DataError("Failed to update profile: ${e.message}", e))
            }
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    override suspend fun registerUser(user: User): Result<Unit> {
        return try {
            userDao.insertUser(user.toDbModel())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save user: ${e.message}"))
=======
    override suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        surname: String
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                if (userRemoteDataSource.isEmailExists(email)) {
                    return@withContext Result.failure(IllegalArgumentException("Email already registered"))
                }

                if (password.length < 6) {
                    return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
                }

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
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    override fun observeUserById(userId: String): Flow<User?> {
        return userDao.getUserById(userId).map { it.toDomain() }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            userDao.deleteUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete user: ${e.message}"))
=======
    override suspend fun deleteUser(userId: String, password: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("test1", "deleteUser start")

                val user = getUserById(userId).getOrNull()
                    ?: return@withContext Result.failure(IllegalStateException("User not found"))
                Log.d("test1", "user $user")

                Log.d("test1", "!verifyPassword(password, user.passwordHash) ${!verifyPassword(password, user.passwordHash)}")
                if (!verifyPassword(password, user.passwordHash)) {
                    return@withContext Result.success(false)
                }

                remoteAuthService.reauthenticateAndDeleteUser(password).getOrThrow()
                localDataSource.deleteUser(userId)

                Result.success(true)
            } catch (e: Exception) {
                Log.d("test1", "e $e")
                Result.failure(Exception("Account deletion failed: ${e.message}"))
            }
>>>>>>> Stashed changes
        }
    }
}