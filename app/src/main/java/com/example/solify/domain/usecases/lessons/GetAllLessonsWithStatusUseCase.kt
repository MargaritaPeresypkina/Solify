package com.example.solify.domain.usecases.lessons

import com.example.solify.domain.entities.lesson.Level
import com.example.solify.domain.entities.progress.Status
import com.example.solify.domain.entities.progress.resolveLessonStatus
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAllLessonsWithStatusUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(userId: String): Flow<List<LessonWithStatus>> {
        val lessonsFlow = lessonRepository.observeAllLessons()
        val lessonsProgressFlow = progressRepository.getAllLessonsProgress(userId)
        val testsProgressFlow = progressRepository.getAllTestsProgress(userId)
        val testIdsByLessonFlow = lessonRepository.observeTestIdsByLesson()

        return combine(
            lessonsFlow,
            lessonsProgressFlow,
            testsProgressFlow,
            testIdsByLessonFlow
        ) { lessons, lessonsProgress, testsProgress, testIdsByLesson ->
            lessons.map { lesson ->
                val lessonProgress = lessonsProgress.find { it.lessonId == lesson.id }
                val lessonTestIds = testIdsByLesson[lesson.id].orEmpty()
                val status = resolveLessonStatus(
                    lessonTestIds = lessonTestIds,
                    lessonProgress = lessonProgress,
                    testsProgress = testsProgress
                )

                LessonWithStatus(
                    id = lesson.id,
                    title = lesson.title,
                    description = lesson.description,
                    level = lesson.level,
                    status = status,
                    order = lesson.order
                )
            }.sortedWith(compareBy({ it.level.ordinal }, { it.order }))
        }
    }
}

data class LessonWithStatus(
    val id: String,
    val title: String,
    val description: String,
    val level: Level,
    val status: Status,
    val order: Int = 0
)
