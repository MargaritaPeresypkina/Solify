package com.example.solify.domain.usecases.progress

import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCompletedExercisesCountUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(userId: String): Flow<Int> =
        progressRepository.observeCompletedExercisesCount(userId)
}
