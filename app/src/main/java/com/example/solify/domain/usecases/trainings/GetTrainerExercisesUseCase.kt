package com.example.solify.domain.usecases.trainings

import com.example.solify.domain.entities.training.Exercise
import com.example.solify.domain.repositories.TrainingRepository
import javax.inject.Inject

class GetTrainerExercisesUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(trainerId: String): Result<TrainerExercisesResult> {
        return trainingRepository.getExercisesForTrainer(trainerId).map { (title, exercises) ->
            TrainerExercisesResult(
                trainerTitle = title,
                exercises = exercises
            )
        }
    }
}

data class TrainerExercisesResult(
    val trainerTitle: String,
    val exercises: List<Exercise>
)
