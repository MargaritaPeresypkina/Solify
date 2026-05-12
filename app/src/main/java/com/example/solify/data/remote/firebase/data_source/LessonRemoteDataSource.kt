package com.example.solify.data.remote.firebase.data_source

import android.util.Log
import com.example.solify.data.remote.firebase.dto.LessonDto
import com.example.solify.data.remote.firebase.mappers.toDomain
import com.example.solify.domain.entities.lesson.Lesson
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getAllLessons(): Result<List<Lesson>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("lessons").get().await()

                val lessons = snapshot.documents.mapNotNull { document ->
                    val lessonDto = document.toObject(LessonDto::class.java)
                    lessonDto?.copy(id = document.id) ?: lessonDto
                }

                Log.d("LessonRemote", "Loaded ${lessons.size} lessons")

                val sortedLessons = lessons.sortedWith(
                    compareBy<LessonDto> { it.level }
                        .thenBy { it.order }
                )

                Result.success(sortedLessons.map { it.toDomain() })
            } catch (e: Exception) {
                Log.e("LessonRemote", "Error loading lessons", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getLessonById(lessonId: String): Result<Lesson> {
        return withContext(Dispatchers.IO) {
            try {
                val document = firestore.collection("lessons").document(lessonId).get().await()
                val lessonDto = document.toObject(LessonDto::class.java)

                if (lessonDto != null && lessonDto.id.isNotEmpty()) {
                    Result.success(lessonDto.toDomain())
                } else {
                    Result.failure(Exception("Lesson not found: $lessonId"))
                }
            } catch (e: Exception) {
                Log.e("LessonRemote", "Error loading lesson $lessonId", e)
                Result.failure(e)
            }
        }
    }
}