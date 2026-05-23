package com.example.solify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.solify.data.local.db_models.ExerciseAnswerOptionDbModel
import com.example.solify.data.local.db_models.ExerciseDbModel
import com.example.solify.data.local.db_models.TrainerDbModel
import com.example.solify.data.local.db_models.TrainingDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    @Query("SELECT * FROM trainings ORDER BY category, id")
    fun getAllTrainings(): Flow<List<TrainingDbModel>>

    @Query("SELECT * FROM trainings WHERE category = :category ORDER BY id")
    fun getTrainingsByCategory(category: String): Flow<List<TrainingDbModel>>

    @Query("SELECT * FROM trainings WHERE id = :trainingId")
    suspend fun getTrainingById(trainingId: String): TrainingDbModel?

    @Query("SELECT * FROM trainers WHERE trainingId = :trainingId ORDER BY id")
    suspend fun getTrainersByTraining(trainingId: String): List<TrainerDbModel>

    @Query("SELECT * FROM trainers WHERE id = :trainerId")
    suspend fun getTrainerById(trainerId: String): TrainerDbModel?

    @Query("SELECT * FROM exercises WHERE trainerId = :trainerId ORDER BY id")
    suspend fun getExercisesByTrainer(trainerId: String): List<ExerciseDbModel>

    @Query("SELECT * FROM exercise_answer_options WHERE exerciseId = :exerciseId ORDER BY id")
    suspend fun getAnswerOptionsByExercise(exerciseId: String): List<ExerciseAnswerOptionDbModel>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: String): ExerciseDbModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainings(trainings: List<TrainingDbModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainers(trainers: List<TrainerDbModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseDbModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseAnswerOptions(options: List<ExerciseAnswerOptionDbModel>)
}
