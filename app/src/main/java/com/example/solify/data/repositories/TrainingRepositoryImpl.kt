package com.example.solify.data.repositories

import com.example.solify.data.dao.TrainingDao
import com.example.solify.data.mappers.toDomain
import com.example.solify.data.models.ExerciseWithOptions
import com.example.solify.data.models.TrainingWithAllData
import com.example.solify.domain.entities.training.Exercise
import com.example.solify.domain.entities.training.Training
import com.example.solify.domain.repositories.TrainingRepository
import com.example.solify.domain.utils.value
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepositoryImpl @Inject constructor(
    private val trainingDao: TrainingDao
) : TrainingRepository {

    override suspend fun getAllTrainings(): Result<List<Training>?> {
        return try {
            val trainingsDb = trainingDao.getAllTrainings().value()
            val trainings = trainingsDb?.map { trainingDb ->
                val exercisesDb = trainingDao.getExercisesByTraining(trainingDb.id)
                val exercisesWithOptions = exercisesDb.map { exerciseDb ->
                    val options = trainingDao.getAnswerOptionsByExercise(exerciseDb.id)
                    ExerciseWithOptions(
                        exercise = exerciseDb,
                        options = options
                    )
                }
                TrainingWithAllData(
                    training = trainingDb,
                    exercises = exercisesWithOptions
                ).toDomain()
            }
            Result.success(trainings)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load trainings: ${e.message}"))
        }
    }

    override suspend fun getTrainingById(trainingId: String): Result<Training> {
        return try {
            val trainingDb = trainingDao.getTrainingById(trainingId)
                ?: return Result.failure(IllegalArgumentException("Training not found"))

            val exercisesDb = trainingDao.getExercisesByTraining(trainingId)
            val exercisesWithOptions = exercisesDb.map { exerciseDb ->
                val options = trainingDao.getAnswerOptionsByExercise(exerciseDb.id)
                ExerciseWithOptions(
                    exercise = exerciseDb,
                    options = options
                )
            }

            val trainingData = TrainingWithAllData(
                training = trainingDb,
                exercises = exercisesWithOptions
            )

            Result.success(trainingData.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load training: ${e.message}"))
        }
    }

    override suspend fun getExerciseById(exerciseId: String): Result<Exercise> {
        return try {
            // TODO
            Result.failure(IllegalStateException("Not implemented"))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load exercise: ${e.message}"))
        }
    }

    override suspend fun playAudio(audioUrl: String): Result<Unit> {
        return try {
            // TODO
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to play audio: ${e.message}"))
        }
    }
}



