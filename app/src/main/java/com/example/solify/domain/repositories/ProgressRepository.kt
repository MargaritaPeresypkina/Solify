package com.example.solify.domain.repositories

import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.entities.progress.UserProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    // UserProgress — return null, if progress doesn't exist
    fun getUserProgress(userId: String): Flow<UserProgress?>

    // LessonProgress
    fun getLessonProgress(userId: String, lessonId: String): Flow<LessonProgress?>
    fun getAllLessonsProgress(userId: String): Flow<List<LessonProgress>>

    // TestProgress
    fun getTestProgress(userId: String, lessonId: String, testId: String): Flow<TestProgress?>
    fun getAllTestsProgress(userId: String, lessonId: String): Flow<List<TestProgress>>

    // Save progress
    suspend fun saveTestProgress(userId: String, lessonId: String, testId: String, progress: TestProgress)
    suspend fun saveLessonProgress(userId: String, lessonId: String, progress: LessonProgress)
    suspend fun saveUserProgress(progress: UserProgress)

    // Clear Progress
    suspend fun clearTestProgress(userId: String, lessonId: String, testId: String)
    suspend fun clearLessonProgress(userId: String, lessonId: String)

    // Complete mark
    suspend fun markTestAsCompleted(userId: String, lessonId: String, testId: String)
    suspend fun markLessonAsCompleted(userId: String, lessonId: String)
}