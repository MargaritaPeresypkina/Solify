package com.example.solify.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.solify.data.db_models.LessonDbModel
import com.example.solify.data.db_models.TestDbModel
import com.example.solify.data.models.LessonWithContentDbModel
import com.example.solify.data.models.QuestionWithOptionsDbModel
import com.example.solify.data.models.TheoryItemWithContentDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    // Lessons
    @Query("SELECT * FROM lessons")
    fun getAllLessons(): Flow<List<LessonDbModel>>

    @Transaction
    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): LessonWithContentDbModel?

    // Theory
    @Transaction
    @Query("SELECT * FROM theory_items WHERE id = :theoryItemId")
    suspend fun getTheoryItemById(theoryItemId: String): TheoryItemWithContentDbModel?

    // Tests

    @Query("SELECT * FROM tests WHERE id = :testId")
    suspend fun getTestById(testId: String): TestDbModel?

    // Questions
    @Query("SELECT id FROM questions WHERE testId = :testId")
    suspend fun getQuestionsIdsByTest(testId: String): List<String>

    @Transaction
    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: String): QuestionWithOptionsDbModel


    // Для GetHintUseCase
    @Query("SELECT hint FROM questions WHERE id = :questionId")
    suspend fun getHint(questionId: String): String?
}