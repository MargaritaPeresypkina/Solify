package com.example.solify.domain.usecases.progress

import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetLessonProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(
        userId: String,
        lessonId: String
    ): Flow<Result<LessonProgress>> {
        return progressRepository.getLessonProgress(userId, lessonId)
            .map { lessonProgress ->
                if (lessonProgress != null) {
                    Result.success(lessonProgress)
                } else {
                    Result.success(
                        LessonProgress(
                            lessonId = lessonId,
                            completedTests = emptySet(),
                            //testProgress = emptyMap()
                        )
                    )
                }
            }
            .catch { e ->
                emit(Result.failure(Exception("Failed to load lesson progress: ${e.message}")))
            }
    }
}