package com.example.solify.domain.usecases.trainings

import com.example.solify.domain.entities.training.Training
import com.example.solify.domain.repositories.TrainingRepository
import javax.inject.Inject

class GetTrainingByIdUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(trainingId: String): Result<Training> {
        return trainingRepository.getTrainingById(trainingId)
    }
}