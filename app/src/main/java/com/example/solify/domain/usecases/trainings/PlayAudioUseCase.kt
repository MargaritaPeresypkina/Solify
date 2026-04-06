package com.example.solify.domain.usecases.trainings

import com.example.solify.domain.repositories.TrainingRepository
import javax.inject.Inject

class PlayAudioUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(audioUrl: String): Result<Unit> {
        return trainingRepository.playAudio(audioUrl)
    }
}