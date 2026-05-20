package com.example.solify.domain.usecases.progress

import com.example.solify.domain.repositories.ProgressRepository
import javax.inject.Inject

class SyncTestsProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(userId: String) {
        progressRepository.syncTestsProgress(userId)
    }
}
