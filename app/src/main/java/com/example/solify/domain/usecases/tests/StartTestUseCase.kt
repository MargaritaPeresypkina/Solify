package com.example.solify.domain.usecases.tests

import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import javax.inject.Inject

class StartTestUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(
        userId: String,
        lessonId: String,
        testId: String,
        shuffleQuestions: Boolean = false
    ): Result<Boolean> {
        return try {
            val test = lessonRepository.getTestById(testId, lessonId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Test not found"))

            if (test.questionsIds.isEmpty()) {
                return Result.failure(IllegalArgumentException("Test has no questions"))
            }

            val existingProgress = progressRepository.getCurrentTestProgress(userId, testId)
            if (existingProgress != null) {
                val needsRepair = existingProgress.pendingQuestions.isEmpty() &&
                    existingProgress.completedQuestions.size < test.questionsIds.size
                if (needsRepair) {
                    val repaired = existingProgress.copy(
                        lessonId = lessonId,
                        pendingQuestions = test.questionsIds.filter {
                            !existingProgress.completedQuestions.contains(it)
                        }
                    )
                    progressRepository.saveTestProgress(userId, repaired)
                }
                return Result.success(false)
            }

            val questionOrder = test.questionsIds.toMutableList()
            if (shuffleQuestions) {
                questionOrder.shuffle()
            }

            val initialProgress = TestProgress(
                testId = testId,
                lessonId = lessonId,
                completedQuestions = emptySet(),
                pendingQuestions = questionOrder
            )
            progressRepository.saveTestProgress(userId, initialProgress)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to start test: ${e.message}"))
        }
    }
}