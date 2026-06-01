package com.example.solify.domain.usecases.tests

import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import javax.inject.Inject

class ShufflePendingQuestionsUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val lessonRepository: LessonRepository
) {
    suspend operator fun invoke(
        userId: String,
        lessonId: String,
        testId: String,
        enabled: Boolean,
        currentQuestionId: String?
    ): Result<Unit> {
        return try {
            val progress = progressRepository.getCurrentTestProgress(userId, testId)
                ?: return Result.failure(IllegalStateException("Test not started"))

            val pending = progress.pendingQuestions
            if (pending.isEmpty()) {
                return Result.success(Unit)
            }

            val newPending = if (enabled) {
                shufflePending(pending, currentQuestionId)
            } else {
                restorePendingOrder(lessonId, testId, pending)
            }

            progressRepository.saveTestProgress(
                userId,
                progress.copy(pendingQuestions = newPending)
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to shuffle questions: ${e.message}"))
        }
    }

    private suspend fun restorePendingOrder(
        lessonId: String,
        testId: String,
        pending: List<String>
    ): List<String> {
        val test = lessonRepository.getTestById(testId, lessonId).getOrNull()
            ?: return pending

        val pendingSet = pending.toSet()
        return test.questionsIds.filter { it in pendingSet }
    }

    private fun shufflePending(
        pending: List<String>,
        currentQuestionId: String?
    ): List<String> {
        if (pending.size <= 1) return pending

        val current = currentQuestionId?.takeIf { pending.firstOrNull() == it }
        val rest = if (current != null) pending.drop(1) else pending

        return if (current != null) {
            listOf(current) + rest.shuffled()
        } else {
            pending.shuffled()
        }
    }
}
