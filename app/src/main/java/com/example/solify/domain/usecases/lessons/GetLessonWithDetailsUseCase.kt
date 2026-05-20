package com.example.solify.domain.usecases.lessons

import com.example.solify.domain.entities.progress.Status
import com.example.solify.domain.entities.progress.resolveTestStatus
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetLessonWithDetailsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(
        userId: String,
        lessonId: String
    ): Result<LessonDetails> {
        return try {
            val lesson = lessonRepository.getLessonById(lessonId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Lesson not found"))

            val testsProgress = progressRepository.getAllTestsProgress(userId).first()
            val lessonProgress = progressRepository.getLessonProgress(userId, lessonId).first()

            val testsWithStatus = lesson.tests.map { test ->
                val testProgress = testsProgress.find { it.testId == test.id }
                TestWithStatus(
                    id = test.id,
                    title = test.title,
                    description = test.description,
                    status = resolveTestStatus(test.id, testProgress, lessonProgress)
                )
            }

            val theorySummaries = lesson.theoryItems
                .sortedBy { it.order }
                .map { theoryItem ->
                    TheoryItemSummary(
                        id = theoryItem.id,
                        title = theoryItem.title,
                        description = theoryItem.description
                    )
                }

            Result.success(
                LessonDetails(
                    id = lesson.id,
                    title = lesson.title,
                    description = lesson.description,
                    level = lesson.level,
                    theoryItems = theorySummaries,
                    tests = testsWithStatus
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load lesson details: ${e.message}"))
        }
    }
}

data class TheoryItemSummary(
    val id: String,
    val title: String,
    val description: String
)

data class TestWithStatus(
    val id: String,
    val title: String,
    val description: String,
    val status: Status
)