package com.example.solify.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.solify.data.db_models.LessonProgressDbModel
import com.example.solify.data.db_models.TestProgressDbModel
import com.example.solify.data.db_models.UserProgressDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    // Read

    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    fun getUserProgress(userId: String): Flow<UserProgressDbModel?>

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId AND lessonId = :lessonId")
    fun getLessonProgress(userId: String, lessonId: String): Flow<LessonProgressDbModel?>

    @Query("SELECT * FROM test_progress WHERE userId = :userId AND lessonId = :lessonId AND testId = :testId")
    fun getTestProgress(userId: String, lessonId: String, testId: String): Flow<TestProgressDbModel?>

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId")
    fun getAllLessonsProgress(userId: String): Flow<List<LessonProgressDbModel>>

    @Query("SELECT * FROM test_progress WHERE userId = :userId AND lessonId = :lessonId")
    fun getAllTestsProgress(userId: String, lessonId: String): Flow<List<TestProgressDbModel>>

    // Insert/Update

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProgress(progress: UserProgressDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLessonProgress(progress: LessonProgressDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTestProgress(progress: TestProgressDbModel)

    // Update

    @Query("""
        UPDATE test_progress 
        SET completedQuestions = :completedQuestions,
            pendingQuestions = :pendingQuestions
        WHERE userId = :userId AND lessonId = :lessonId AND testId = :testId
    """)
    suspend fun updateTestProgress(
        userId: String,
        lessonId: String,
        testId: String,
        completedQuestions: String,
        pendingQuestions: String
    )

    @Query("""
        UPDATE lesson_progress 
        SET completedTests = :completedTests
        WHERE userId = :userId AND lessonId = :lessonId
    """)
    suspend fun updateLessonProgress(
        userId: String,
        lessonId: String,
        completedTests: String
    )

    @Query("""
        UPDATE user_progress 
        SET completedLessons = :completedLessons
        WHERE userId = :userId
    """)
    suspend fun updateUserProgress(
        userId: String,
        completedLessons: String
    )

    // Delete

    @Query("DELETE FROM user_progress WHERE userId = :userId")
    suspend fun resetUserProgress(userId: String)

    @Query("DELETE FROM lesson_progress WHERE userId = :userId AND lessonId = :lessonId")
    suspend fun resetLessonProgress(userId: String, lessonId: String)

    @Query("DELETE FROM test_progress WHERE userId = :userId AND lessonId = :lessonId AND testId = :testId")
    suspend fun resetTestProgress(userId: String, lessonId: String, testId: String)
}