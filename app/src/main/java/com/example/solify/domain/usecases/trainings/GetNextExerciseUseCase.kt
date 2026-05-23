package com.example.solify.domain.usecases.trainings

import com.example.solify.domain.entities.training.Exercise
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.repositories.TrainingRepository
import javax.inject.Inject

class GetNextExerciseUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(userId: String, trainerId: String): Result<Exercise?> {
        return try {
            val progress = progressRepository.getCurrentExerciseProgress(userId, trainerId)
                ?: return Result.failure(
                    IllegalStateException("Trainer session not started. Call StartTrainerSessionUseCase first.")
                )

            val pendingExercises = progress.pendingExercises
            if (pendingExercises.isEmpty()) {
                return Result.success(null)
            }

            val nextExerciseId = pendingExercises.first()
            trainingRepository.getExerciseById(nextExerciseId)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get next exercise: ${e.message}"))
        }
    }
}
