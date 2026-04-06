package com.example.solify.domain.usecases.progress

import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllLessonsProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<LessonProgress>>> {
        return progressRepository.getAllLessonsProgress(userId) //List(lessonId, set - completedTests)
            .map { lessonsProgress ->
                Result.success(lessonsProgress)
            }
            .catch { e ->
                emit(Result.failure(Exception("Failed to load lessons progress: ${e.message}")))
            }
    }
}