package com.example.solify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.solify.data.local.db_models.AnswerOptionDbModel
import com.example.solify.data.local.db_models.LessonDbModel
import com.example.solify.data.local.db_models.QuestionDbModel
import com.example.solify.data.local.db_models.TestDbModel
import com.example.solify.data.local.db_models.TheoryContentDbModel
import com.example.solify.data.local.db_models.TheoryItemDbModel
import com.example.solify.data.local.models.LessonWithContentDbModel
import com.example.solify.data.local.models.QuestionWithOptionsDbModel
import com.example.solify.data.local.models.TheoryItemWithContentDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    // Lessons
    @Query("SELECT * FROM lessons ORDER BY level ASC, `order` ASC")
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

    @Query("SELECT * FROM tests")
    fun observeAllTests(): Flow<List<TestDbModel>>

    // Questions
    @Query("SELECT id FROM questions WHERE testId = :testId")
    suspend fun getQuestionsIdsByTest(testId: String): List<String>

    @Transaction
    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: String): QuestionWithOptionsDbModel?


    // Для GetHintUseCase
    @Query("SELECT hint FROM questions WHERE id = :questionId")
    suspend fun getHint(questionId: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLesson(lesson: LessonDbModel): Long

    @Update
    suspend fun updateLesson(lesson: LessonDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheoryItems(items: List<TheoryItemDbModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheoryContents(contents: List<TheoryContentDbModel>)

    @Query("DELETE FROM theory_contents WHERE theoryItemId = :theoryItemId")
    suspend fun deleteTheoryContentsForItem(theoryItemId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTests(tests: List<TestDbModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswerOptions(options: List<AnswerOptionDbModel>)

    @Query("SELECT COUNT(*) FROM questions WHERE id = :questionId")
    suspend fun questionExists(questionId: String): Int

    @Query("DELETE FROM theory_items WHERE lessonId = :lessonId")
    suspend fun deleteTheoryItemsForLesson(lessonId: String)

    @Query("DELETE FROM tests WHERE lessonId = :lessonId")
    suspend fun deleteTestsForLesson(lessonId: String)

    @Query("DELETE FROM lessons")
    suspend fun clearAllLessons()
}