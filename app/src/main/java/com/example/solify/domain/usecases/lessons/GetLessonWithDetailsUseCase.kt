package com.example.solify.domain.usecases.lessons

import com.example.solify.domain.entities.lesson.Level
import com.example.solify.domain.entities.progress.Status
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
    ): Result<LessonWithDetails> {
        return try {
            val lesson = lessonRepository.getLessonById(lessonId).getOrNull() //id title description level {id title desc, ...} {id title desc, ...}
                ?: return Result.failure(IllegalArgumentException("Lesson not found"))
            
            // List(testId set - completedQuestions list - pendingQuestions)
            val testsProgress = progressRepository.getAllTestsProgress(userId, lessonId).first()
            
            val testsWithStatus = lesson.tests.map { test ->
                val testProgress = testsProgress.find { it.testId == test.id }
                val status = calculateTestStatus(testProgress)
                
                TestWithStatus(
                    id = test.id,
                    title = test.title,
                    description = test.description,
                    status = status
                )
            }
            
            val theorySummaries = lesson.theoryItems.map { theoryItem ->
                TheoryItemSummary(
                    id = theoryItem.id,
                    title = theoryItem.title,
                    description = theoryItem.description
                )
            }
            
            Result.success(
                LessonWithDetails(
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
    
    private fun calculateTestStatus(
        testProgress: com.example.solify.domain.entities.progress.TestProgress?
    ): Status {
        return when {
            testProgress == null -> Status.NOT_STARTED
            testProgress.completedQuestions.isNotEmpty() -> Status.IN_PROGRESS
            else -> Status.NOT_STARTED
        }
    }
}

data class LessonWithDetails(
    val id: String,
    val title: String,
    val description: String,
    val level: Level,
    val theoryItems: List<TheoryItemSummary>,
    val tests: List<TestWithStatus>
)

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