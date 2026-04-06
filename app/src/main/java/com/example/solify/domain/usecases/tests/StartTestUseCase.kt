package com.example.solify.domain.usecases.tests

import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.repositories.TestRepository
import com.example.solify.domain.utils.value
import javax.inject.Inject

class StartTestUseCase @Inject constructor(
    private val testRepository: TestRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(
        userId: String,
        lessonId: String,
        testId: String
    ): Result<StartTestResult> {
        return try {
            val test = testRepository.getTestById(testId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Test not found"))

            val lessonProgress = progressRepository.getLessonProgress(userId, lessonId).value()
            val isTestCompleted = lessonProgress?.completedTests?.contains(testId) == true

            if (isTestCompleted) {
                return Result.success(
                    StartTestResult(
                        testId = testId,
                        testTitle = test.title,
                        totalQuestions = test.questions.size,
                        currentQuestion = null,
                        isCompleted = true
                    )
                )
            }

            var progress = progressRepository.getTestProgress(userId, lessonId, testId).value()

            if (progress == null) {
                val initialProgress = TestProgress(
                    testId = testId,
                    completedQuestions = emptySet(),
                    pendingQuestions = test.questions.map { it.id }
                )
                progressRepository.saveTestProgress(userId, lessonId, testId, initialProgress)
                progress = initialProgress
            }

            val firstQuestionId = progress.pendingQuestions.firstOrNull()
            val firstQuestion = if (firstQuestionId != null) {
                testRepository.getQuestionById(firstQuestionId).getOrNull()
            } else {
                null
            }

            Result.success(
                StartTestResult(
                    testId = testId,
                    testTitle = test.title,
                    totalQuestions = test.questions.size,
                    currentQuestion = firstQuestion,
                    isCompleted = false,
                    completedQuestions = progress.completedQuestions.size
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to start test: ${e.message}"))
        }
    }
}

data class StartTestResult(
    val testId: String,
    val testTitle: String,
    val totalQuestions: Int,
    val currentQuestion: Question?,
    val isCompleted: Boolean,
    val completedQuestions: Int = 0
)