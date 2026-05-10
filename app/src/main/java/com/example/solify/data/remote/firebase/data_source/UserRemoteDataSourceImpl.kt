package com.example.solify.data.remote.firebase.data_source

import android.util.Log
import com.example.solify.data.remote.firebase.dto.UserDto
import com.example.solify.data.remote.firebase.mappers.toDomain
import com.example.solify.domain.entities.user.User
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val supabaseStorage: Storage
) {

    suspend fun getUserProfile(userId: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                Log.d("test", "document $document")
                val userDto = document.toObject(UserDto::class.java)
                Log.d("test", "userDto $userDto")

                if (userDto != null && userDto.id.isNotEmpty()) {
                    Result.success(userDto.toDomain())
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun updateUserProfile(user: User): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val updates = hashMapOf<String, Any>(
                    "name" to user.name,
                    "surname" to user.surname,
                    "email" to user.email
                )
                
                user.avatarUrl?.let {
                    updates["avatarUrl"] = it
                }

                firestore.collection("users").document(user.id).update(updates).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun isEmailExists(email: String): Boolean {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun uploadAvatar(userId: String, imageFile: File): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val fileExtension = imageFile.extension
                val fileName = "avatars/$userId/${UUID.randomUUID()}.$fileExtension"

                val s = supabaseStorage.from("SolifyAvatars").upload(
                    path = fileName,
                    data = imageFile.readBytes()
                )
                val publicUrl = supabaseStorage.from("SolifyAvatars").publicUrl(fileName)
                firestore.collection("users").document(userId).update("avatarUrl", publicUrl).await()
                
                Result.success(publicUrl)
            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }
    
    suspend fun deleteAvatar(userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                val currentAvatarUrl = userDoc.getString("avatarUrl")
                
                if (currentAvatarUrl != null && currentAvatarUrl.isNotEmpty()) {
                    val path = extractPathFromUrl(currentAvatarUrl)
                    if (path != null) {
                        supabaseStorage.from("SolifyAvatars").delete(listOf(path))
                    }
                }
                
                firestore.collection("users").document(userId).update("avatarUrl", "").await()
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun extractPathFromUrl(url: String): String? {
        return try {
            val pattern = "/storage/v1/object/public/avatars/(.+)".toRegex()
            pattern.find(url)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}