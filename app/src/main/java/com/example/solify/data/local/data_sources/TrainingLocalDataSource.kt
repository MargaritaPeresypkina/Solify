package com.example.solify.data.local.data_sources

import com.example.solify.data.local.dao.TrainingDao
import com.example.solify.data.local.db_models.TrainingDbModel
import com.example.solify.data.local.mappers.toDomain
import com.example.solify.data.local.models.ExerciseWithOptions
import com.example.solify.data.local.models.TrainingWithExercises
import com.example.solify.domain.entities.training.Exercise
import com.example.solify.domain.entities.training.Training
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingLocalDataSource @Inject constructor(
    private val trainingDao: TrainingDao
) {

    fun getAllTrainings(): Flow<List<Training>> {
        return trainingDao.getAllTrainings().map { trainingsDb ->
            trainingsDb.map { trainingDb ->
                try {
                    convertToDomain(trainingDb)
                } catch (e: Exception) {
                    throw DataSourceException.MappingError("Failed to map training ${trainingDb.id}", e)
                }
            }
        }
    }

    suspend fun getTrainingById(trainingId: String): Training {
        return try {
            val trainingDb = trainingDao.getTrainingById(trainingId)
                ?: throw DomainException.NotFound("Training not found: $trainingId")
            convertToDomain(trainingDb)
        } catch (e: DomainException.NotFound) {
            throw e
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get training $trainingId", e)
        }
    }

    fun getTrainingsByCategory(category: String): Flow<List<Training>> {
        return trainingDao.getTrainingsByCategory(category).map { trainingsDb ->
            trainingsDb.map { trainingDb ->
                try {
                    convertToDomain(trainingDb)
                } catch (e: Exception) {
                    throw DataSourceException.MappingError("Failed to map training by category", e)
                }
            }
        }
    }


    suspend fun getExerciseById(exerciseId: String): Exercise {
        return try {
            val exerciseDb = trainingDao.getExerciseById(exerciseId)
                ?: throw DomainException.NotFound("Exercise not found: $exerciseId")

            val options = trainingDao.getAnswerOptionsByExercise(exerciseId)
            ExerciseWithOptions(exercise = exerciseDb, options = options).toDomain()
        } catch (e: DomainException.NotFound) {
            throw e
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get exercise $exerciseId", e)
        }
    }


    private suspend fun convertToDomain(trainingDb: TrainingDbModel): Training {
        val exercisesDb = trainingDao.getExercisesByTraining(trainingDb.id)
        val exercisesWithOptions = exercisesDb.map { exerciseDb ->
            val options = trainingDao.getAnswerOptionsByExercise(exerciseDb.id)
            ExerciseWithOptions(exercise = exerciseDb, options = options)
        }

        val trainingData = TrainingWithExercises(
            training = trainingDb,
            exercises = exercisesWithOptions
        )

        return trainingData.toDomain()
    }
}
