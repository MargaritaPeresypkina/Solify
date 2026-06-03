package com.example.solify.domain.usecases.progress

import com.example.solify.domain.repositories.ProgressRepository
import javax.inject.Inject

class SyncAllProgressUseCase @Inject constructor(
    private val syncTestsProgressUseCase: SyncTestsProgressUseCase,
    private val syncLessonsProgressUseCase: SyncLessonsProgressUseCase,
    private val syncDailyActivityUseCase: SyncDailyActivityUseCase,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(userId: String) {
        runCatching { syncTestsProgressUseCase(userId) }
        runCatching { syncLessonsProgressUseCase(userId) }
        runCatching { syncDailyActivityUseCase(userId) }
        runCatching { progressRepository.syncCompletedExercisesCount(userId) }
    }
}
