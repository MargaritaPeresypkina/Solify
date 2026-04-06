package com.example.solify.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.solify.data.db_models.AnswerOptionDbModel
import com.example.solify.data.db_models.LessonDbModel
import com.example.solify.data.db_models.QuestionDbModel
import com.example.solify.data.db_models.TestDbModel
import com.example.solify.data.db_models.TheoryContentDbModel
import com.example.solify.data.db_models.TheoryItemDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    // Lessons
    @Query("SELECT * FROM lessons")
    fun getAllLessons(): Flow<List<LessonDbModel>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): LessonDbModel?

    @Query("SELECT * FROM lessons WHERE level = :level ORDER BY id")
    fun getLessonsByLevel(level: String): Flow<List<LessonDbModel>>

    // Theory
    @Query("SELECT * FROM theory_items WHERE id = :theoryItemId")
    suspend fun getTheoryItemById(theoryItemId: String): TheoryItemDbModel?

    @Query("SELECT * FROM theory_items WHERE lessonId = :lessonId")
    suspend fun getTheoryItemsByLesson(lessonId: String): List<TheoryItemDbModel>

    @Query("SELECT * FROM theory_contents WHERE theoryItemId = :theoryItemId")
    suspend fun getTheoryContentsByItem(theoryItemId: String): List<TheoryContentDbModel>

    // Tests
    @Query("SELECT * FROM tests WHERE lessonId = :lessonId")
    suspend fun getTestsByLesson(lessonId: String): List<TestDbModel>

    @Query("SELECT * FROM tests WHERE lessonId = :lessonId")
    fun getTestsByLessonFlow(lessonId: String): Flow<List<TestDbModel>>

    @Query("SELECT * FROM questions WHERE testId = :testId")
    suspend fun getQuestionsByTest(testId: String): List<QuestionDbModel>

    @Query("SELECT * FROM answer_options WHERE questionId = :questionId")
    suspend fun getAnswerOptionsByQuestion(questionId: String): List<AnswerOptionDbModel>

    @Query("SELECT * FROM tests WHERE id = :testId")
    suspend fun getTestById(testId: String): TestDbModel?

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: String): QuestionDbModel?

    // Для GetHintUseCase
    @Query("SELECT hint FROM questions WHERE id = :questionId AND testId = :testId")
    suspend fun getHint(questionId: String, testId: String): String?
}