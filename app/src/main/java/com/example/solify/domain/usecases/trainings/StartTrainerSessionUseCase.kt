package com.example.solify.domain.usecases.trainings

import com.example.solify.domain.entities.progress.ExerciseProgress
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.repositories.TrainingRepository
import javax.inject.Inject

class StartTrainerSessionUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(userId: String, trainerId: String): Result<Boolean> {
        return try {
            val (_, exercises) = trainingRepository.getExercisesForTrainer(trainerId).getOrElse { error ->
                return Result.failure(error)
            }
            val exerciseIds = exercises.map { it.id }
            if (exerciseIds.isEmpty()) {
                return Result.failure(IllegalArgumentException("Trainer has no exercises"))
            }

            val existingProgress = progressRepository.getCurrentExerciseProgress(userId, trainerId)
            if (existingProgress != null) {
                val needsRepair = existingProgress.pendingExercises.isEmpty() &&
                    existingProgress.completedExercises.size < exerciseIds.size
                if (needsRepair) {
                    val repaired = existingProgress.copy(
                        pendingExercises = exerciseIds.filter { exerciseId ->
                            !existingProgress.completedExercises.contains(exerciseId)
                        }
                    )
                    progressRepository.saveExerciseProgress(userId, repaired)
                }
                return Result.success(false)
            }

            val initialProgress = ExerciseProgress(
                trainerId = trainerId,
                completedExercises = emptySet(),
                pendingExercises = exerciseIds.toMutableList()
            )
            progressRepository.saveExerciseProgress(userId, initialProgress)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to start trainer session: ${e.message}"))
        }
    }
}
