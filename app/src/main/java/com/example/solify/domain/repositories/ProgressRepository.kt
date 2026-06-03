package com.example.solify.domain.repositories

import com.example.solify.domain.entities.progress.DailyActivity
import com.example.solify.domain.entities.progress.TestsCompletionSnapshot
import com.example.solify.domain.entities.progress.ExerciseProgress
import com.example.solify.domain.entities.progress.WeeklyActivityDay
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.TestProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    // Lesson Progress
    fun getLessonProgress(userId: String, lessonId: String): Flow<LessonProgress?>
    fun getAllLessonsProgress(userId: String): Flow<List<LessonProgress>>
    suspend fun saveLessonProgress(userId: String, progress: LessonProgress)
    suspend fun clearLessonProgress(userId: String, lessonId: String)
    suspend fun syncLessonsProgress(userId: String)
    suspend fun syncTestsProgress(userId: String)
    suspend fun syncDailyActivity(userId: String)

    // Daily activity
    fun observeWeeklyActivity(userId: String): Flow<List<WeeklyActivityDay>>
    suspend fun recordTestCompletionForToday(userId: String)

    // Test Progress
    fun getTestProgress(userId: String, testId: String): Flow<TestProgress?>
    fun observeTestProgress(userId: String, testId: String): Flow<TestProgress?>
    suspend fun getCurrentTestProgress(userId: String, testId: String): TestProgress?
    suspend fun isTestCompleted(userId: String, lessonId: String, testId: String): Boolean
    fun getAllTestsProgress(userId: String): Flow<List<TestProgress>>
    fun observeTestsProgressForLesson(userId: String, lessonId: String): Flow<List<TestProgress>>
    suspend fun saveTestProgress(userId: String, progress: TestProgress)
    suspend fun clearTestProgress(userId: String, testId: String)

    // Exercise Progress
    fun observeExerciseProgress(userId: String, trainerId: String): Flow<ExerciseProgress?>
    suspend fun getCurrentExerciseProgress(userId: String, trainerId: String): ExerciseProgress?
    suspend fun saveExerciseProgress(userId: String, progress: ExerciseProgress)
    suspend fun clearExerciseProgress(userId: String, trainerId: String)

    // Actions
    suspend fun completeQuestion(userId: String, lessonId: String, testId: String, questionId: String)
    suspend fun completeTest(userId: String, lessonId: String, testId: String)
    suspend fun getNextPendingTest(userId: String, lessonId: String): String?

    suspend fun fetchTestsCompletionSnapshot(
        userId: String,
        allTestIds: Set<String>
    ): TestsCompletionSnapshot

    suspend fun fetchRemoteCompletedTestIds(userId: String): Set<String>
}