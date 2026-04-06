package com.example.solify.domain.usecases.lessons

import com.example.solify.domain.entities.lesson.Level
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.Status
import com.example.solify.domain.entities.progress.UserProgress
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
        val lessonsFlow = lessonRepository.getAllLessons() //List(id title description level)
        val userProgressFlow = progressRepository.getUserProgress(userId) //userId, completedLessons Set<String>
        val lessonsProgressFlow = progressRepository.getAllLessonsProgress(userId) //List(lessonId completedTests Set<String>)

        return combine(lessonsFlow, userProgressFlow, lessonsProgressFlow)
        { lessons, userProgress, lessonsProgress ->
            lessons.map { lesson ->
                val lessonProgress = lessonsProgress.find { it.lessonId == lesson.id }

                val status = calculateLessonStatus(
                    lessonId = lesson.id,
                    userProgress = userProgress,
                    lessonProgress = lessonProgress
                )

                LessonWithStatus(
                    id = lesson.id,
                    title = lesson.title,
                    description = lesson.description,
                    level = lesson.level,
                    status = status
                )
            }
        }
    }

    private fun calculateLessonStatus(
        lessonId: String,
        userProgress: UserProgress?,
        lessonProgress: LessonProgress?
    ): Status {
        return when {
            userProgress?.completedLessons?.contains(lessonId) == true -> Status.COMPLETED
            lessonProgress != null && lessonProgress.completedTests.isNotEmpty() -> Status.IN_PROGRESS
            else -> Status.NOT_STARTED
        }
    }
}

data class LessonWithStatus(
    val id: String,
    val title: String,
    val description: String,
    val level: Level,
    val status: Status
)
