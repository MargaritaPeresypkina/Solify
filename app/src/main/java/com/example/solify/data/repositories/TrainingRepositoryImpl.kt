package com.example.solify.data.repositories

import com.example.solify.data.local.dao.TrainingDao
import com.example.solify.data.local.mappers.toDomain
import com.example.solify.data.local.models.ExerciseWithOptions
import com.example.solify.data.local.models.TrainingWithTrainers
import com.example.solify.data.remote.firebase.data_source.TrainingRemoteDataSource
import com.example.solify.data.remote.firebase.mappers.toDbModel
import com.example.solify.data.remote.firebase.mappers.toListItemDomain
import com.example.solify.domain.entities.training.Exercise
import com.example.solify.domain.entities.training.Training
import com.example.solify.domain.repositories.TrainingRepository
import com.example.solify.domain.utils.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepositoryImpl @Inject constructor(
    private val trainingDao: TrainingDao,
    private val remoteDataSource: TrainingRemoteDataSource
) : TrainingRepository {

    override fun observeAllTrainings(): Flow<List<Training>> {
        return trainingDao.getAllTrainings().map { trainingsDb ->
            trainingsDb.map { it.toListItemDomain() }
        }
    }

    override suspend fun syncTrainings(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteTrainings = remoteDataSource.getAllTrainings().getOrElse {
                return@withContext Result.failure(it)
            }
            trainingDao.insertTrainings(remoteTrainings.map { it.toDbModel() })
            remoteTrainings.forEach { training ->
                if (training.trainers.isNotEmpty()) {
                    trainingDao.insertTrainers(
                        training.trainers.map { trainer -> trainer.toDbModel(training.id) }
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to sync trainings: ${e.message}", e))
        }
    }

    override suspend fun getAllTrainings(): Result<List<Training>?> {
        return try {
            val trainingsDb = trainingDao.getAllTrainings().value()
            val trainings = trainingsDb?.map { trainingDb ->
                val trainersDb = trainingDao.getTrainersByTraining(trainingDb.id)
                TrainingWithTrainers(
                    training = trainingDb,
                    trainers = trainersDb
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

            val trainersDb = trainingDao.getTrainersByTraining(trainingId)
            val training = TrainingWithTrainers(
                training = trainingDb,
                trainers = trainersDb
            ).toDomain()

            Result.success(training)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load training: ${e.message}"))
        }
    }

    override suspend fun getExerciseById(exerciseId: String): Result<Exercise> {
        return try {
            val exerciseDb = trainingDao.getExerciseById(exerciseId)
                ?: return Result.failure(IllegalArgumentException("Exercise not found"))

            val options = trainingDao.getAnswerOptionsByExercise(exerciseId)
            val exercise = ExerciseWithOptions(
                exercise = exerciseDb,
                options = options
            ).toDomain()

            Result.success(exercise)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load exercise: ${e.message}"))
        }
    }

    override suspend fun playAudio(audioUrl: String): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to play audio: ${e.message}"))
        }
    }
}
