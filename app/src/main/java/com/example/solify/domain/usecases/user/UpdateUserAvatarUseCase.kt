package com.example.solify.domain.usecases.user

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class UpdateUserAvatarUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val context: Context
) {
    suspend operator fun invoke(imageUri: String): Result<String> {
        return try {
            val userId = sessionManager.getCurrentUserId()
                ?: return Result.failure(IllegalStateException("Not logged in"))

            val currentUser = userRepository.getUserById(userId).getOrNull()
                ?: return Result.failure(IllegalStateException("User not found"))

            val savedPath = copyImageToAppStorage(imageUri.toUri(), context)

            val updatedUser = currentUser.copy(avatarUrl = savedPath)

            userRepository.updateUser(updatedUser).getOrNull()
                ?: return Result.failure(Exception("Failed to save avatar"))

            Result.success(savedPath)
        } catch (e: Exception) {
            Result.failure(Exception("Avatar upload failed: ${e.message}"))
        }
    }
}

private fun copyImageToAppStorage(uri: Uri, context: Context): String {
    val fileName = "avatar_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, fileName)

    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    } ?: throw Exception("Failed to copy image")

    return file.absolutePath
}