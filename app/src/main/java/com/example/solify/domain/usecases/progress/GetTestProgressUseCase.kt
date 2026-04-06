package com.example.solify.domain.usecases.progress

import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTestProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(
        userId: String,
        lessonId: String,
        testId: String
    ): Flow<Result<TestProgress>> {
        return progressRepository.getTestProgress(userId, lessonId, testId)
            .map { testProgress ->
                if (testProgress != null) {
                    Result.success(testProgress)
                } else {
                    Result.success(
                        TestProgress(
                            testId = testId,
                            completedQuestions = emptySet(),
                            pendingQuestions = emptyList()
                        )
                    )
                }
            }
            .catch { e ->
                emit(Result.failure(Exception("Failed to load test progress: ${e.message}")))
            }
    }
}