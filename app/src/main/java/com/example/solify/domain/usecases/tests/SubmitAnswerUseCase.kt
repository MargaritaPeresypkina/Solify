package com.example.solify.domain.usecases.tests

import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.utils.value
import javax.inject.Inject

class SubmitAnswerUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(
        userId: String,
        lessonId: String,
        testId: String,
        questionId: String,
        selectedOptionId: String
    ): Result<SubmitAnswerResult> {
        return try {
            val question = lessonRepository.getQuestionById(questionId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Question not found"))

            val isCorrect = question.correctOptionId == selectedOptionId

            val currentProgress = progressRepository.getTestProgress(userId, lessonId, testId).value()
                ?: return Result.failure(IllegalStateException("Test not started"))

            val updatedProgress = if (isCorrect) {
                TestProgress(
                    testId = testId,
                    completedQuestions = currentProgress.completedQuestions + questionId,
                    pendingQuestions = currentProgress.pendingQuestions.filter { it != questionId }
                )
            } else {
                val newPending = currentProgress.pendingQuestions.toMutableList()
                newPending.remove(questionId)
                newPending.add(questionId)
                TestProgress(
                    testId = testId,
                    completedQuestions = currentProgress.completedQuestions,
                    pendingQuestions = newPending
                )
            }

            val test = lessonRepository.getTestById(testId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Test not found"))

            val isTestCompleted = updatedProgress.completedQuestions.size == test.questionsIds.size
            if (isTestCompleted) {
                progressRepository.clearTestProgress(userId, lessonId, testId)
                progressRepository.markTestAsCompleted(userId, lessonId, testId)
                checkAndMarkLessonCompleted(userId, lessonId)
            } else {
                progressRepository.saveTestProgress(userId, lessonId, testId, updatedProgress)
            }

            Result.success(
                SubmitAnswerResult(
                    isCorrect = isCorrect,
                    correctOptionId = question.correctOptionId,
                    isTestCompleted = isTestCompleted,
                    completedQuestions = updatedProgress.completedQuestions.size,
                    totalQuestions = test.questionsIds.size
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to submit answer: ${e.message}"))
        }
    }

    private suspend fun checkAndMarkLessonCompleted(userId: String, lessonId: String) {
        val testsProgress = progressRepository.getAllTestsProgress(userId, lessonId).value() ?: return
        val lessonProgress = progressRepository.getLessonProgress(userId, lessonId).value()
        val alreadyCompleted = lessonProgress?.completedTests ?: emptySet()

        val allTestsCompleted = testsProgress.all { testProgress ->
            alreadyCompleted.contains(testProgress.testId) || testProgress.completedQuestions.isNotEmpty()
        }

        if (allTestsCompleted && testsProgress.isNotEmpty()) {
            progressRepository.markLessonAsCompleted(userId, lessonId)
        }
    }
}

data class SubmitAnswerResult(
    val isCorrect: Boolean,
    val correctOptionId: String,
    val isTestCompleted: Boolean,
    val completedQuestions: Int,
    val totalQuestions: Int
)