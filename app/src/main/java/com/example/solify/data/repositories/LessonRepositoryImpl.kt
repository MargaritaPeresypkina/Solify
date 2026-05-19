package com.example.solify.data.repositories

import android.util.Log
import com.example.solify.data.local.data_sources.LessonLocalDataSource
import com.example.solify.data.local.db_models.LessonDbModel
import com.example.solify.data.remote.firebase.data_source.LessonRemoteDataSource
import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.entities.lesson.TheoryItem
import com.example.solify.domain.repositories.LessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LessonRepositoryImpl @Inject constructor(
    private val localDataSource: LessonLocalDataSource,
    private val remoteDataSource: LessonRemoteDataSource
) : LessonRepository {

    override fun getAllLessons(): Flow<List<Lesson>> {
        return flow {
            val cachedLessons = localDataSource.getAllLessons()
            emit(cachedLessons.first())
            Log.d("LessonsDebug", "cachedLessons ${cachedLessons.first()}")

    override suspend fun syncLessons(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteLessons = remoteDataSource.getAllLessons().getOrElse { return@withContext Result.failure(it) }
            remoteLessons.forEach { lesson ->
                localDataSource.upsertLesson(
                    LessonDbModel(
                        id = lesson.id,
                        title = lesson.title,
                        description = lesson.description,
                        level = lesson.level.name,
                        order = lesson.order
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to sync lessons: ${e.message}", e))
        }
    }

    override suspend fun getLessonById(lessonId: String): Result<Lesson> {
        return try {
            val remoteLesson = remoteDataSource.getLessonById(lessonId).getOrNull()

            if (remoteLesson != null) {
                val lessonDb = LessonDbModel(
                    id = remoteLesson.id,
                    title = remoteLesson.title,
                    description = remoteLesson.description,
                    level = remoteLesson.level.name,
                    order = remoteLesson.order
                )
                localDataSource.insertLesson(lessonDb)
                Result.success(remoteLesson)
            } else {
                val localLesson = localDataSource.getLessonById(lessonId)
                Result.success(localLesson)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load lesson: ${e.message}"))
        }
    }

    override suspend fun getTheoryItemById(theoryItemId: String): Result<TheoryItem> {
        return try {
            val theoryItem = localDataSource.getTheoryItemById(theoryItemId)
            Result.success(theoryItem)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load theory item: ${e.message}"))
        }
    }

    override suspend fun getTestById(testId: String): Result<Test> {
        return try {
            val test = localDataSource.getTestById(testId)
            Result.success(test)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load test: ${e.message}"))
        }
    }

    override suspend fun getQuestionById(questionId: String): Result<Question> {
        return try {
            val question = localDataSource.getQuestionById(questionId)
            Result.success(question)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load question: ${e.message}"))
        }
    }

    override suspend fun getHint(questionId: String): Result<String> {
        return try {
            val hint = localDataSource.getHint(questionId)
            Result.success(hint)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load hint: ${e.message}"))
        }
    }
}