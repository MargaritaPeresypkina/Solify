package com.example.solify.domain.usecases.tests

import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.utils.value
import javax.inject.Inject

class StartTestUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(
        userId: String,
        lessonId: String,
        testId: String
    ): Result<Boolean> {
        return try {
            val existingProgress = progressRepository.getTestProgress(userId, lessonId, testId).value()

            if (existingProgress != null) {
                return Result.success(false)
            }

            val test = lessonRepository.getTestById(testId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Test not found"))

            val initialProgress = TestProgress(
                testId = testId,
                completedQuestions = emptySet(),
                pendingQuestions = test.questionsIds.toMutableList()
            )
            progressRepository.saveTestProgress(userId, lessonId, testId, initialProgress)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to start test: ${e.message}"))
        }
    }
}