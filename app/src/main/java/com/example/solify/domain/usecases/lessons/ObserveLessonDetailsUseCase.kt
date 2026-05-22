package com.example.solify.domain.usecases.lessons

import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.entities.progress.resolveTestStatus
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.presentation.debug.AgentDebugLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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

        val lessonTestIds = lesson.tests.map { it.id }.toSet()

        progressRepository.syncTestsProgress(userId)
        progressRepository.syncLessonsProgress(userId)

        combine(
            progressRepository.getAllTestsProgress(userId),
            progressRepository.observeTestsProgressForLesson(userId, lessonId),
            progressRepository.getLessonProgress(userId, lessonId)
        ) { allTestsProgress, joinedTestsProgress, lessonProgress ->
            val fromAll = allTestsProgress.filter { it.testId in lessonTestIds }
            val mergedTestsProgress = (fromAll + joinedTestsProgress)
                .groupBy { it.testId }
                .map { (_, progresses) -> progresses.maxBy { it.completedQuestions.size } }
                .filter { it.testId in lessonTestIds }

            // #region agent log
            AgentDebugLog.log(
                hypothesisId = "A",
                location = "ObserveLessonDetailsUseCase",
                message = "progress sources merged",
                data = mapOf(
                    "lessonId" to lessonId,
                    "lessonTestIdsCount" to lessonTestIds.size,
                    "allProgressCount" to allTestsProgress.size,
                    "fromAllCount" to fromAll.size,
                    "joinedCount" to joinedTestsProgress.size,
                    "mergedCount" to mergedTestsProgress.size,
                    "mergedCompletedTotal" to mergedTestsProgress.sumOf { it.completedQuestions.size },
                    "lessonCompletedTestsCount" to (lessonProgress?.completedTests?.size ?: 0)
                ),
                runId = "post-fix"
            )
            // #endregion

            Result.success(mapLessonDetails(lesson, mergedTestsProgress, lessonProgress))
        }
            .distinctUntilChanged { old, new ->
                old.getOrNull()?.tests == new.getOrNull()?.tests &&
                    old.getOrNull()?.id == new.getOrNull()?.id
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
            val status = resolveTestStatus(test.id, testProgress, lessonProgress)
            // #region agent log
            AgentDebugLog.log(
                hypothesisId = "B",
                location = "ObserveLessonDetailsUseCase",
                message = "test status mapped",
                data = mapOf(
                    "lessonId" to lesson.id,
                    "testId" to test.id,
                    "status" to status.name,
                    "completedQ" to (testProgress?.completedQuestions?.size ?: 0),
                    "foundProgress" to (testProgress != null)
                )
            )
            // #endregion
            TestWithStatus(
                id = test.id,
                title = test.title,
                description = test.description,
                status = status
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
