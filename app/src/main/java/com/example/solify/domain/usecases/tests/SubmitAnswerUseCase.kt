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
            val question = lessonRepository.getQuestionById(questionId, testId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Question not found"))

            val isCorrect = question.correctOptionId == selectedOptionId

            val currentProgress = progressRepository.getCurrentTestProgress(userId, testId)
                ?: return Result.failure(IllegalStateException("Test not started"))

            val updatedProgress = if (isCorrect) {
                TestProgress(
                    testId = testId,
                    lessonId = lessonId,
                    completedQuestions = currentProgress.completedQuestions + questionId,
                    pendingQuestions = currentProgress.pendingQuestions.filter { it != questionId }
                )
            } else {
                val newPending = currentProgress.pendingQuestions.toMutableList()
                newPending.remove(questionId)
                newPending.add(questionId)
                TestProgress(
                    testId = testId,
                    lessonId = lessonId,
                    completedQuestions = currentProgress.completedQuestions,
                    pendingQuestions = newPending
                )
            }

            val test = lessonRepository.getTestById(testId, lessonId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Test not found"))

            val isTestCompleted = updatedProgress.completedQuestions.size == test.questionsIds.size
            if (isTestCompleted) {
                progressRepository.completeTest(userId, lessonId, testId)
                progressRepository.clearTestProgress(userId, testId)
            } else {
                progressRepository.saveTestProgress(userId, updatedProgress)
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

}

data class SubmitAnswerResult(
    val isCorrect: Boolean,
    val correctOptionId: String,
    val isTestCompleted: Boolean,
    val completedQuestions: Int,
    val totalQuestions: Int
)