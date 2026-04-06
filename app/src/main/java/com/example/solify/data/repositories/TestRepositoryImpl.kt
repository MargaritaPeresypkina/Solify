package com.example.solify.data.repositories

import com.example.solify.data.dao.LessonDao
import com.example.solify.data.mappers.toDomain
import com.example.solify.data.models.QuestionWithOptions
import com.example.solify.data.models.TestWithQuestions
import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.repositories.TestRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao
) : TestRepository {

    override suspend fun getTestById(testId: String): Result<Test> {
        return try {
            val testDb = lessonDao.getTestById(testId)
                ?: return Result.failure(IllegalArgumentException("Test not found"))

            val questionsDb = lessonDao.getQuestionsByTest(testId)
            val questionsWithOptions = questionsDb.map { questionDb ->
                val options = lessonDao.getAnswerOptionsByQuestion(questionDb.id)
                QuestionWithOptions(
                    question = questionDb,
                    options = options
                )
            }

            val testData = TestWithQuestions(
                test = testDb,
                questions = questionsWithOptions
            )

            Result.success(testData.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load test: ${e.message}"))
        }
    }

    override suspend fun getQuestionById(questionId: String): Result<Question> {
        return try {
            val questionDb = lessonDao.getQuestionById(questionId)
                ?: return Result.failure(IllegalArgumentException("Question not found"))

            val options = lessonDao.getAnswerOptionsByQuestion(questionId)
            val question = QuestionWithOptions(
                question = questionDb,
                options = options
            ).toDomain()

            Result.success(question)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load question: ${e.message}"))
        }
    }

    override suspend fun getHint(questionId: String, testId: String): Result<String> {
        return try {
            val hint = lessonDao.getHint(questionId, testId)
                ?: return Result.failure(IllegalArgumentException("Hint not found"))
            Result.success(hint)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load hint: ${e.message}"))
        }
    }
}