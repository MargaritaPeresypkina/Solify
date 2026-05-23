package com.example.solify.data.repositories

import com.example.solify.data.local.dao.TrainingDao
import com.example.solify.data.local.mappers.toDbModel as localToDbModel
import com.example.solify.data.local.mappers.toDomain
import com.example.solify.data.local.models.ExerciseWithOptions
import com.example.solify.data.local.models.TrainingWithTrainers
import com.example.solify.data.remote.firebase.data_source.ExerciseRemoteDataSource
import com.example.solify.data.remote.firebase.data_source.TrainingRemoteDataSource
import com.example.solify.data.remote.firebase.mappers.toDbModel as remoteToDbModel
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
    private val remoteDataSource: TrainingRemoteDataSource,
    private val exerciseRemoteDataSource: ExerciseRemoteDataSource
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
            trainingDao.insertTrainings(remoteTrainings.map { it.remoteToDbModel() })

            remoteTrainings.forEach { training ->
                if (training.trainers.isNotEmpty()) {
                    trainingDao.insertTrainers(
                        training.trainers.map { trainer -> trainer.remoteToDbModel(training.id) }
                    )
                    training.trainers.forEach { trainer ->
                        trainer.exercisesIds.forEach { exerciseId ->
                            cacheExerciseFromRemote(exerciseId, trainer.id)
                        }
                    }
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
            Result.success(loadExercise(exerciseId))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load exercise: ${e.message}"))
        }
    }

    override suspend fun getExercisesForTrainer(trainerId: String): Result<Pair<String, List<Exercise>>> {
        return try {
            val trainerDb = trainingDao.getTrainerById(trainerId)
                ?: return Result.failure(IllegalArgumentException("Trainer not found"))

            val exercises = if (trainerDb.exercisesIds.isNotEmpty()) {
                trainerDb.exercisesIds.mapNotNull { exerciseId ->
                    loadExerciseWithFallback(exerciseId, trainerId)
                }
            } else {
                trainingDao.getExercisesByTrainer(trainerId).map { exerciseDb ->
                    val options = trainingDao.getAnswerOptionsByExercise(exerciseDb.id)
                    ExerciseWithOptions(exercise = exerciseDb, options = options).toDomain()
                }
            }

            if (exercises.isEmpty()) {
                return Result.failure(IllegalArgumentException("No exercises for trainer"))
            }

            Result.success(trainerDb.title to exercises)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load trainer exercises: ${e.message}"))
        }
    }

    private suspend fun loadExerciseWithFallback(exerciseId: String, trainerId: String): Exercise? {
        val localExercise = trainingDao.getExerciseById(exerciseId)
        if (localExercise != null) {
            val options = trainingDao.getAnswerOptionsByExercise(exerciseId)
            return ExerciseWithOptions(exercise = localExercise, options = options).toDomain()
        }

        return cacheExerciseFromRemote(exerciseId, trainerId)
    }

    private suspend fun cacheExerciseFromRemote(exerciseId: String, trainerId: String): Exercise? {
        val remoteExercise = exerciseRemoteDataSource.getExerciseById(exerciseId, trainerId)
            .getOrElse { return null }

        cacheExercise(remoteExercise, trainerId)
        return remoteExercise
    }

    private suspend fun cacheExercise(exercise: Exercise, trainerId: String) {
        trainingDao.insertExercises(listOf(exercise.localToDbModel(trainerId)))
        trainingDao.insertExerciseAnswerOptions(
            exercise.options.map { option -> option.localToDbModel(exercise.id) }
        )
    }

    private suspend fun loadExercise(exerciseId: String): Exercise {
        val exerciseDb = trainingDao.getExerciseById(exerciseId)
            ?: throw IllegalArgumentException("Exercise not found")
        val options = trainingDao.getAnswerOptionsByExercise(exerciseId)
        return ExerciseWithOptions(exercise = exerciseDb, options = options).toDomain()
    }

    override suspend fun playAudio(audioUrl: String): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to play audio: ${e.message}"))
        }
    }
}
