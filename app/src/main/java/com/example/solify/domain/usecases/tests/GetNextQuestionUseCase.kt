package com.example.solify.domain.usecases.tests

import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.utils.value
import com.example.solify.presentation.debug.AgentDebugLog
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
            val progress = progressRepository.getCurrentTestProgress(userId, testId)
                ?: return Result.failure(IllegalStateException("Test not started. Call StartTestUseCase first."))

            val pendingQuestions = progress.pendingQuestions
            // #region agent log
            AgentDebugLog.log(
                hypothesisId = "B",
                location = "GetNextQuestionUseCase",
                message = "pending queue state",
                data = mapOf(
                    "testId" to testId,
                    "pendingSize" to pendingQuestions.size,
                    "completedSize" to progress.completedQuestions.size,
                    "firstPending" to pendingQuestions.firstOrNull()
                )
            )
            // #endregion
            if (pendingQuestions.isEmpty()) {
                return Result.success(null)
            }

            val nextQuestionId = pendingQuestions.first()
            val nextQuestion = lessonRepository.getQuestionById(nextQuestionId, testId).getOrNull()
            // #region agent log
            AgentDebugLog.log(
                hypothesisId = "C",
                location = "GetNextQuestionUseCase",
                message = "question load result",
                data = mapOf(
                    "nextQuestionId" to nextQuestionId,
                    "loaded" to (nextQuestion != null)
                )
            )
            // #endregion

            Result.success(nextQuestion)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get next question: ${e.message}"))
        }
    }
}