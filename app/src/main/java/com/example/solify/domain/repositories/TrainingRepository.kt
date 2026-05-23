package com.example.solify.domain.repositories

import com.example.solify.domain.entities.training.Exercise
import com.example.solify.domain.entities.training.Training
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {

    fun observeAllTrainings(): Flow<List<Training>>

    suspend fun syncTrainings(): Result<Unit>

    suspend fun getAllTrainings(): Result<List<Training>?>

    suspend fun getTrainingById(trainingId: String): Result<Training>

    suspend fun getExerciseById(exerciseId: String): Result<Exercise>

    suspend fun playAudio(audioUrl: String): Result<Unit>
}