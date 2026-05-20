package com.example.solify.data.remote.firebase.data_source

import android.util.Log
import com.example.solify.data.remote.firebase.dto.LessonDto
import com.example.solify.data.remote.firebase.dto.TestDto
import com.example.solify.data.remote.firebase.dto.TheoryContentDto
import com.example.solify.data.remote.firebase.dto.TheoryItemDto
import com.example.solify.data.remote.firebase.mappers.toDomain
import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.entities.lesson.TheoryItem
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
                val lessonRef = firestore.collection("lessons").document(lessonId)
                val document = lessonRef.get().await()
                val lessonDto = document.toObject(LessonDto::class.java)?.copy(id = document.id)

                if (lessonDto == null || lessonDto.id.isEmpty()) {
                    return@withContext Result.failure(Exception("Lesson not found: $lessonId"))
                }

                val theoryItems = loadTheoryItems(lessonRef.path)
                val tests = loadTests(lessonRef.path)

                Result.success(
                    lessonDto.toDomain().copy(
                        theoryItems = theoryItems,
                        tests = tests
                    )
                )
            } catch (e: Exception) {
                Log.e("LessonRemote", "Error loading lesson $lessonId", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getTheoryItemById(lessonId: String, theoryItemId: String): Result<TheoryItem> {
        return withContext(Dispatchers.IO) {
            try {
                val theoryItemPath =
                    "lessons/$lessonId/theory_items/$theoryItemId"
                val document = firestore.document(theoryItemPath).get().await()
                val theoryItemDto = document.toObject(TheoryItemDto::class.java)
                    ?.copy(id = document.id)

                if (theoryItemDto == null || theoryItemDto.id.isEmpty()) {
                    return@withContext Result.failure(
                        Exception("Theory item not found: $theoryItemId")
                    )
                }

                val contents = loadTheoryContents(theoryItemPath)
                Result.success(theoryItemDto.toDomain(contents))
            } catch (e: Exception) {
                Log.e("LessonRemote", "Error loading theory item $theoryItemId", e)
                Result.failure(e)
            }
        }
    }

    private suspend fun loadTheoryItems(lessonPath: String): List<TheoryItem> {
        val snapshot = firestore.collection("$lessonPath/theory_items").get().await()
        return snapshot.documents.mapNotNull { document ->
            val theoryItemDto = document.toObject(TheoryItemDto::class.java)
                ?.copy(id = document.id) ?: return@mapNotNull null
            val contents = loadTheoryContents(document.reference.path)
            theoryItemDto.toDomain(contents)
        }.sortedBy { it.order }
    }

    private suspend fun loadTheoryContents(theoryItemPath: String): List<TheoryContentDto> {
        val snapshot = firestore.collection("$theoryItemPath/theory_content").get().await()
        return snapshot.documents.mapNotNull { document ->
            val type = document.getString("type") ?: return@mapNotNull null
            val content = document.getString("content") ?: return@mapNotNull null
            val order = document.getLong("order")?.toInt()
                ?: (document.get("order") as? Number)?.toInt()
                ?: 0
            TheoryContentDto(type = type, content = content, order = order)
        }.sortedBy { it.order }
    }

    private suspend fun loadTests(lessonPath: String): List<Test> {
        val snapshot = firestore.collection("$lessonPath/tests").get().await()
        return snapshot.documents.mapNotNull { document ->
            val questionsIds = (document.get("questionsIds") as? List<*>)
                ?.filterIsInstance<String>()
                .orEmpty()
            document.toObject(TestDto::class.java)
                ?.copy(id = document.id, questionsIds = questionsIds)
                ?.toDomain()
        }
    }
}