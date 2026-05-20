package com.example.solify.domain.usecases.lessons

import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.entities.progress.resolveTestStatus
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveLessonDetailsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(userId: String, lessonId: String): Flow<Result<LessonDetails>> = channelFlow {
        val lesson = lessonRepository.getLessonById(lessonId).getOrElse { error ->
            send(Result.failure(error))
            return@channelFlow
        }

        combine(
            progressRepository.getAllTestsProgress(userId),
            progressRepository.getLessonProgress(userId, lessonId)
        ) { testsProgress, lessonProgress ->
            Result.success(mapLessonDetails(lesson, testsProgress, lessonProgress))
        }
            .catch { error ->
                emit(Result.failure(Exception("Failed to load lesson progress: ${error.message}", error)))
            }
            .collect { result -> send(result) }
    }

    private fun mapLessonDetails(
        lesson: Lesson,
        testsProgress: List<TestProgress>,
        lessonProgress: LessonProgress?
    ): LessonDetails {
        val theoryItems = lesson.theoryItems
            .sortedBy { it.order }
            .map { item ->
                TheoryItemSummary(
                    id = item.id,
                    title = item.title,
                    description = item.description
                )
            }

        val tests = lesson.tests.map { test ->
            val testProgress = testsProgress.find { it.testId == test.id }
            TestWithStatus(
                id = test.id,
                title = test.title,
                description = test.description,
                status = resolveTestStatus(test.id, testProgress, lessonProgress)
            )
        }

        return LessonDetails(
            id = lesson.id,
            title = lesson.title,
            description = lesson.description,
            level = lesson.level,
            theoryItems = theoryItems,
            tests = tests
        )
    }
}

data class LessonDetails(
    val id: String,
    val title: String,
    val description: String,
    val level: com.example.solify.domain.entities.lesson.Level,
    val theoryItems: List<TheoryItemSummary>,
    val tests: List<TestWithStatus>
)
