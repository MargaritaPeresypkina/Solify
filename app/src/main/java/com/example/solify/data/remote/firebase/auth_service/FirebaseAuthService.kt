package com.example.solify.data.remote.firebase.auth_service

import android.util.Log
import com.example.solify.data.remote.firebase.dto.UserDto
import com.example.solify.data.remote.firebase.mappers.toDomain
import com.example.solify.data.remote.firebase.mappers.toDto
import com.example.solify.domain.entities.user.User
import com.example.solify.domain.utils.hashPassword
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun registerUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("User creation failed"))
        } catch (e: FirebaseAuthException) {
            Result.failure(mapFirebaseAuthError(e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("Login failed"))
        } catch (e: FirebaseAuthException) {
            Result.failure(mapFirebaseAuthError(e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logoutUser(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    suspend fun deleteUser(): Result<Unit> {
//        return try {
//            val currentUser = firebaseAuth.currentUser
//                ?: return Result.failure(Exception("No user logged in"))
//
//            currentUser.uid.let { userId ->
//                firestore.collection("users").document(userId).delete().await()
//            }
//
//            currentUser.delete().await()
//
//            Result.success(Unit)
//        } catch (e: FirebaseAuthException) {
//            Result.failure(mapFirebaseAuthError(e))
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

    suspend fun reauthenticateAndDeleteUser(password: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("No user logged in"))
            Log.d("test1", "currentUser $currentUser")
            val email = currentUser.email
                ?: return Result.failure(Exception("User email not found"))
            Log.d("test1", "email $email")

            // Создаем учётные данные для повторной аутентификации
            val credential = EmailAuthProvider.getCredential(email, password)
            Log.d("test1", "credential $credential")

            // Повторная аутентификация (обязательно для удаления)
            val reauthenticate = currentUser.reauthenticate(credential).await()
            Log.d("test1", "reauthenticate $reauthenticate")



            // Удаляем документ из Firestore
            val a = firestore.collection("users").document(currentUser.uid).delete().await()

            val delete = currentUser.delete().await()
            Log.d("test1", "delete $delete")

            Log.d("test1", "a $a")
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.failure(mapFirebaseAuthError(e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.id)
                .set(user.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapFirebaseAuthError(e: FirebaseAuthException): Exception {
        return when (e.errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> IllegalArgumentException("Email already registered")
            "ERROR_WRONG_PASSWORD" -> IllegalArgumentException("Invalid password")
            "ERROR_USER_NOT_FOUND" -> IllegalArgumentException("User not found")
            "ERROR_WEAK_PASSWORD" -> IllegalArgumentException("Password is too weak")
            "ERROR_INVALID_EMAIL" -> IllegalArgumentException("Invalid email format")
            else -> Exception(e.message ?: "Authentication failed")
        }
    }
}