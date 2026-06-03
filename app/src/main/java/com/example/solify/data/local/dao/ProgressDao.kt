package com.example.solify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.solify.data.local.db_models.DailyActivityDbModel
import com.example.solify.data.local.db_models.ExerciseProgressDbModel
import com.example.solify.data.local.db_models.LessonProgressDbModel
import com.example.solify.data.local.db_models.TestProgressDbModel
import com.example.solify.data.local.db_models.UserProgressDbModel
import com.example.solify.data.local.models.TestProgressWithLessonDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    // Daily activity

    @Query(
        """
        SELECT * FROM daily_activity
        WHERE userId = :userId AND date >= :sinceDate
        ORDER BY date ASC
        """
    )
    fun observeDailyActivitySince(userId: String, sinceDate: String): Flow<List<DailyActivityDbModel>>

    @Query(
        """
        SELECT * FROM daily_activity
        WHERE userId = :userId AND date >= :sinceDate
        ORDER BY date ASC
        """
    )
    suspend fun getDailyActivitySince(userId: String, sinceDate: String): List<DailyActivityDbModel>

    @Query("SELECT * FROM daily_activity WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getDailyActivity(userId: String, date: String): DailyActivityDbModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyActivity(activity: DailyActivityDbModel)

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE id = :userId)")
    suspend fun hasUser(userId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM lessons WHERE id = :lessonId)")
    suspend fun hasLesson(lessonId: String): Boolean

    // Read

    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    fun getUserProgress(userId: String): Flow<UserProgressDbModel?>

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId AND lessonId = :lessonId")
    fun getLessonProgress(userId: String, lessonId: String): Flow<LessonProgressDbModel?>

    @Query("SELECT * FROM test_progress WHERE userId = :userId AND testId = :testId")
    fun getTestProgress(userId: String, testId: String): Flow<TestProgressDbModel?>

    @Query("SELECT * FROM exercise_progress WHERE userId = :userId AND trainerId = :trainerId")
    fun getExerciseProgress(userId: String, trainerId: String): Flow<ExerciseProgressDbModel?>

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId")
    fun getAllLessonsProgress(userId: String): Flow<List<LessonProgressDbModel>>

    @Query("SELECT * FROM test_progress WHERE userId = :userId")
    fun getAllTestsProgress(userId: String): Flow<List<TestProgressDbModel>>

    @Query(
        """
        SELECT
            tp.userId AS userId,
            tp.testId AS testId,
            tp.completedQuestions AS completedQuestions,
            tp.pendingQuestions AS pendingQuestions,
            t.lessonId AS lessonId,
            tp.status AS status
        FROM test_progress AS tp
        LEFT JOIN tests AS t ON tp.testId = t.id
        WHERE tp.userId = :userId
        """
    )
    fun getAllTestsProgressWithLesson(userId: String): Flow<List<TestProgressWithLessonDbModel>>

    @Query(
        """
        SELECT
            tp.userId AS userId,
            tp.testId AS testId,
            tp.completedQuestions AS completedQuestions,
            tp.pendingQuestions AS pendingQuestions,
            t.lessonId AS lessonId,
            tp.status AS status
        FROM test_progress AS tp
        INNER JOIN tests AS t ON tp.testId = t.id
        WHERE tp.userId = :userId AND t.lessonId = :lessonId
        """
    )
    fun observeTestsProgressForLesson(
        userId: String,
        lessonId: String
    ): Flow<List<TestProgressWithLessonDbModel>>

    // Insert/Update

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProgress(progress: UserProgressDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLessonProgress(progress: LessonProgressDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTestProgress(progress: TestProgressDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateExerciseProgress(progress: ExerciseProgressDbModel)

    // Update

    @Query("""
        UPDATE test_progress 
        SET completedQuestions = :completedQuestions,
            pendingQuestions = :pendingQuestions
        WHERE userId = :userId AND testId = :testId
    """)
    suspend fun updateTestProgress(
        userId: String,
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

    @Query("DELETE FROM test_progress WHERE userId = :userId AND testId = :testId")
    suspend fun resetTestProgress(userId: String, testId: String)

    @Query("DELETE FROM exercise_progress WHERE userId = :userId AND trainerId = :trainerId")
    suspend fun resetExerciseProgress(userId: String, trainerId: String)
}