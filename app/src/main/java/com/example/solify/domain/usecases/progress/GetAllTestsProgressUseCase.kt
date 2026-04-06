package com.example.solify.domain.usecases.progress

import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllTestsProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(
        userId: String,
        lessonId: String
    ): Flow<Result<List<TestProgress>>> {
        return progressRepository.getAllTestsProgress(userId, lessonId)
            .map { testsProgress ->
                Result.success(testsProgress)
            }
            .catch { e ->
                emit(Result.failure(Exception("Failed to load tests progress: ${e.message}")))
            }
    }
}