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

            val tempFile = saveImageToTempFile(imageUri.toUri())

            val result = userRepository.updateUserAvatar(userId, tempFile)

            tempFile.delete()

            result
        } catch (e: Exception) {
            Result.failure(Exception("Avatar upload failed: ${e.message}"))
        }
    }

    private fun saveImageToTempFile(uri: Uri): File {
        val tempFile = File.createTempFile("avatar_upload", ".jpg")
        val contentResolver = context.contentResolver

        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Failed to read image")

        return tempFile
    }
}