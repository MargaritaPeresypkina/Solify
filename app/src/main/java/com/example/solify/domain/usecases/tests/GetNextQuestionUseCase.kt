package com.example.solify.domain.usecases.tests

import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.utils.value
import javax.inject.Inject

class GetNextQuestionUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(
        userId: String,
        lessonId: String,
        testId: String
    ): Result<Question?> {
        return try {
            val progress = progressRepository.getTestProgress(userId, lessonId, testId).value()
                ?: return Result.failure(IllegalStateException("Test not started. Call StartTestUseCase first."))

            val pendingQuestions = progress.pendingQuestions
            if (pendingQuestions.isEmpty()) {
                return Result.success(null)
            }

            val nextQuestionId = pendingQuestions.first()
            val nextQuestion = lessonRepository.getQuestionById(nextQuestionId).getOrNull()

            Result.success(nextQuestion)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get next question: ${e.message}"))
        }
    }
}