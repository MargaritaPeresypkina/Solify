package com.example.solify.domain.usecases.progress

import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.utils.value
import javax.inject.Inject

class EnsureLessonProgressUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(userId: String, lessonId: String): Result<Unit> {
        return try {
            val lesson = lessonRepository.getLessonById(lessonId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Lesson not found"))

            val existing = progressRepository.getLessonProgress(userId, lessonId).value()
            val completedTests = existing?.completedTests ?: emptySet()
            val pendingTests = lesson.tests
                .map { it.id }
                .filter { testId -> !completedTests.contains(testId) }

            if (existing != null &&
                existing.pendingTests == pendingTests &&
                existing.completedTests == completedTests
            ) {
                return Result.success(Unit)
            }

            progressRepository.saveLessonProgress(
                userId,
                LessonProgress(
                    lessonId = lessonId,
                    completedTests = completedTests,
                    pendingTests = pendingTests
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to ensure lesson progress: ${e.message}"))
        }
    }
}
