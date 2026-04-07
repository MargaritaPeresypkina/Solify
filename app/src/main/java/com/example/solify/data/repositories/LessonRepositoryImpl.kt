package com.example.solify.data.repositories

import com.example.solify.data.dao.LessonDao
import com.example.solify.data.mappers.toDomain
import com.example.solify.data.mappers.toLessonsDomain
import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.entities.lesson.TheoryItem
import com.example.solify.domain.repositories.LessonRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao
) : LessonRepository {

    override fun getAllLessons(): Flow<List<Lesson>> {
        return lessonDao.getAllLessons().map { it.toLessonsDomain() }
    }

    override suspend fun getLessonById(lessonId: String): Result<Lesson> {
        return try {
            val lessonDb = lessonDao.getLessonById(lessonId)
                ?: return Result.failure(IllegalArgumentException("Lesson not found"))

            Result.success(
                lessonDb.toDomain()
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load lesson: ${e.message}"))
        }
    }

    override suspend fun getTheoryItemById(theoryItemId: String): Result<TheoryItem> {
        return try {
            val theoryItemDb = lessonDao.getTheoryItemById(theoryItemId)
                ?: return Result.failure(IllegalArgumentException("Theory item not found"))

            Result.success(
                theoryItemDb.toDomain()
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load theory item: ${e.message}"))
        }
    }

    override suspend fun getTestById(testId: String): Result<Test> {
        return try {
            val testDb = lessonDao.getTestById(testId)
                ?: return Result.failure(IllegalArgumentException("Test not found"))

            val questionsIds = lessonDao.getQuestionsIdsByTest(testId)

            Result.success(testDb.toDomain(questionsIds))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load test: ${e.message}"))
        }
    }

    override suspend fun getQuestionById(questionId: String): Result<Question> {
        return try {
            val questionDb = lessonDao.getQuestionById(questionId)

            Result.success(questionDb.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load test: ${e.message}"))
        }
    }


    override suspend fun getHint(questionId: String): Result<String> {
        return try {
            val hint = lessonDao.getHint(questionId)
                ?: return Result.failure(IllegalArgumentException("Hint not found"))
            Result.success(hint)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load hint: ${e.message}"))
        }
    }
}