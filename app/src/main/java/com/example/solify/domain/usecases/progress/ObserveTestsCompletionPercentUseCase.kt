package com.example.solify.domain.usecases.progress

import com.example.solify.domain.entities.progress.TestsCompletionOverview
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveTestsCompletionPercentUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val calculateTestsCompletionOverviewUseCase: CalculateTestsCompletionOverviewUseCase
) {
    operator fun invoke(userId: String): Flow<TestsCompletionOverview> {
        return progressRepository.getAllLessonsProgress(userId)
            .mapLatest {
                calculateTestsCompletionOverviewUseCase(userId)
            }
            .onStart {
                emit(calculateTestsCompletionOverviewUseCase(userId))
            }
    }
}
