package com.example.solify.domain.usecases.lessons

import android.util.Log
import com.example.solify.domain.entities.lesson.Level
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.Status
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetAllLessonsWithStatusUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(userId: String): Flow<List<LessonWithStatus>> {
        val lessonsFlow = lessonRepository.getAllLessons()
        val lessonsProgressFlow = progressRepository.getAllLessonsProgress(userId)

        return combine(lessonsFlow, lessonsProgressFlow)
        { lessons, lessonsProgress ->
            lessons.map { lesson ->
                val lessonProgress = lessonsProgress.find { it.lessonId == lesson.id }

                val status = calculateLessonStatus(lessonProgress)

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

    private fun calculateLessonStatus(lessonProgress: LessonProgress?): Status {
        return when {
            lessonProgress == null -> Status.NOT_STARTED
            lessonProgress.pendingTests.size == 1 && lessonProgress.pendingTests.get(0) == "" && lessonProgress.completedTests.isNotEmpty() -> Status.COMPLETED
            lessonProgress.completedTests.isNotEmpty() -> Status.IN_PROGRESS
            else -> Status.NOT_STARTED
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