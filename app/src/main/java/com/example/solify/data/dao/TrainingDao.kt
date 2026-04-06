package com.example.solify.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.solify.data.db_models.ExerciseAnswerOptionDbModel
import com.example.solify.data.db_models.ExerciseDbModel
import com.example.solify.data.db_models.TrainingDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {

    // Для GetAllTrainingsUseCase
    @Query("SELECT * FROM trainings ORDER BY category, id")
    fun getAllTrainings(): Flow<List<TrainingDbModel>>

    @Query("SELECT * FROM trainings WHERE category = :category ORDER BY id")
    fun getTrainingsByCategory(category: String): Flow<List<TrainingDbModel>>

    // Для GetTrainingByIdUseCase
    @Query("SELECT * FROM trainings WHERE id = :trainingId")
    suspend fun getTrainingById(trainingId: String): TrainingDbModel?

    // Для получения упражнений тренировки
    @Query("SELECT * FROM exercises WHERE trainingId = :trainingId ORDER BY id")
    suspend fun getExercisesByTraining(trainingId: String): List<ExerciseDbModel>

    // Для получения вариантов ответов упражнения
    @Query("SELECT * FROM exercise_answer_options WHERE exerciseId = :exerciseId ORDER BY id")
    suspend fun getAnswerOptionsByExercise(exerciseId: String): List<ExerciseAnswerOptionDbModel>
}