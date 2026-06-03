package com.example.solify.domain.usecases.trainings

import com.example.solify.domain.entities.progress.ExerciseProgress
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.repositories.TrainingRepository
import javax.inject.Inject

class SubmitExerciseAnswerUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(
        userId: String,
        trainerId: String,
        exerciseId: String,
        selectedOptionId: String
    ): Result<SubmitExerciseAnswerResult> {
        return try {
            val exercise = trainingRepository.getExerciseById(exerciseId).getOrElse { error ->
                return Result.failure(error)
            }

            val isCorrect = exercise.correctAnswerId == selectedOptionId
            val currentProgress = progressRepository.getCurrentExerciseProgress(userId, trainerId)
                ?: return Result.failure(IllegalStateException("Trainer session not started"))

            val (_, allExercises) = trainingRepository.getExercisesForTrainer(trainerId).getOrElse { error ->
                return Result.failure(error)
            }
            val totalExercises = allExercises.size

            val wasAlreadyCompleted = exerciseId in currentProgress.completedExercises

            val updatedProgress = if (isCorrect) {
                ExerciseProgress(
                    trainerId = trainerId,
                    completedExercises = currentProgress.completedExercises + exerciseId,
                    pendingExercises = currentProgress.pendingExercises.filter { it != exerciseId }
                )
            } else {
                val newPending = currentProgress.pendingExercises.toMutableList()
                newPending.remove(exerciseId)
                newPending.add(exerciseId)
                ExerciseProgress(
                    trainerId = trainerId,
                    completedExercises = currentProgress.completedExercises,
                    pendingExercises = newPending
                )
            }

            if (isCorrect && !wasAlreadyCompleted) {
                progressRepository.recordCompletedExercise(userId)
            }

            val isSessionCompleted = updatedProgress.completedExercises.size == totalExercises
            if (isSessionCompleted) {
                progressRepository.clearExerciseProgress(userId, trainerId)
            } else {
                progressRepository.saveExerciseProgress(userId, updatedProgress)
            }

            Result.success(
                SubmitExerciseAnswerResult(
                    isCorrect = isCorrect,
                    correctOptionId = exercise.correctAnswerId,
                    isSessionCompleted = isSessionCompleted,
                    completedExercises = updatedProgress.completedExercises.size,
                    totalExercises = totalExercises
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to submit exercise answer: ${e.message}"))
        }
    }
}

data class SubmitExerciseAnswerResult(
    val isCorrect: Boolean,
    val correctOptionId: String,
    val isSessionCompleted: Boolean,
    val completedExercises: Int,
    val totalExercises: Int
)
