package com.example.solify.domain.usecases.progress

import com.example.solify.domain.entities.progress.UserProgress
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(userId: String): Flow<Result<UserProgress>> {
        return progressRepository.getUserProgress(userId)
            .map { userProgress ->
                if (userProgress != null) {
                    Result.success(userProgress)
                } else {
                    Result.success(UserProgress(
                        userId = userId,
                        completedLessons = emptySet(),
                        //lessonProgress = emptyMap()
                    ))
                }
            }
            .catch { e ->
                emit(Result.failure(Exception("Failed to load user progress: ${e.message}")))
            }
    }
}