package com.example.solify.domain.usecases.tests

import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.repositories.TestRepository
import com.example.solify.domain.utils.value
import javax.inject.Inject

class SubmitAnswerUseCase @Inject constructor(
    private val testRepository: TestRepository,
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
            val question = testRepository.getQuestionById(questionId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Question not found"))

            val currentProgress = progressRepository.getTestProgress(userId, lessonId, testId).value()
                ?: return Result.failure(IllegalStateException("Test not started"))

            val isCorrect = question.correctOptionId == selectedOptionId

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

            val test = testRepository.getTestById(testId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Test not found"))

            val isTestCompleted = updatedProgress.completedQuestions.size == test.questions.size

            // Сохраняем прогресс
            if (isTestCompleted) {
                progressRepository.clearTestProgress(userId, lessonId, testId)
                progressRepository.markTestAsCompleted(userId, lessonId, testId)
                checkAndMarkLessonCompleted(userId, lessonId)
            } else {
                progressRepository.saveTestProgress(userId, lessonId, testId, updatedProgress)
            }

            val nextQuestion = if (!isTestCompleted && updatedProgress.pendingQuestions.isNotEmpty()) {
                val nextId = updatedProgress.pendingQuestions.first()
                testRepository.getQuestionById(nextId).getOrNull()
            } else {
                null
            }

            Result.success(
                SubmitAnswerResult(
                    isCorrect = isCorrect,
                    correctOptionId = question.correctOptionId,
                    nextQuestion = nextQuestion,
                    isTestCompleted = isTestCompleted,
                    completedQuestions = updatedProgress.completedQuestions.size,
                    totalQuestions = test.questions.size
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

        val allTestsCompleted = testsProgress.all { test ->
            alreadyCompleted.contains(test.testId) || test.completedQuestions.isNotEmpty()
        }

        if (allTestsCompleted && testsProgress.isNotEmpty()) {
            progressRepository.markLessonAsCompleted(userId, lessonId)
        }
    }
}

data class SubmitAnswerResult(
    val isCorrect: Boolean,
    val correctOptionId: String,
    val nextQuestion: Question?,
    val isTestCompleted: Boolean,
    val completedQuestions: Int,
    val totalQuestions: Int
)