package com.example.solify.data.remote.firebase.data_source

import android.util.Log
import com.example.solify.data.remote.firebase.dto.UserProgressDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getUserProgress(userId: String): Result<UserProgressDto> {
        return withContext(Dispatchers.IO) {
            try {
                val document = firestore
                    .collection("users")
                    .document(userId)
                    .collection("user_progress")
                    .document("progress")
                    .get()
                    .await()
                Log.d("LessonsDebug", "getUserProgress $document")

                val progress = document.toObject(UserProgressDto::class.java)
                Log.d("LessonsDebug", "progress ${progress?.completedLessons}")

                Result.success(progress ?: UserProgressDto())
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error loading user progress", e)
                Result.failure(e)
            }
        }
    }

    suspend fun updateUserProgress(userId: String, completedLessons: List<String>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val data = hashMapOf(
                    "completedLessons" to completedLessons
                )
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("user_progress")
                    .document("progress")
                    .set(data)
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error updating user progress", e)
                Result.failure(e)
            }
        }
    }
}