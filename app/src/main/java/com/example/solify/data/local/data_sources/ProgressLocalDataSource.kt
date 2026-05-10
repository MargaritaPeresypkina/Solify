package com.example.solify.data.local.data_sources

import com.example.solify.data.local.dao.ProgressDao
import com.example.solify.data.local.db_models.LessonProgressDbModel
import com.example.solify.data.local.db_models.TestProgressDbModel
import com.example.solify.data.local.db_models.UserProgressDbModel
import com.example.solify.data.local.mappers.toDomain
import com.example.solify.data.local.mappers.toLessonsDomain
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.entities.progress.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressLocalDataSource @Inject constructor(
    private val progressDao: ProgressDao
) {
    
    // User Progress
    fun getUserProgress(userId: String): Flow<UserProgress?> =
       progressDao.getUserProgress(userId).map { progress ->
           try {
               progress?.toDomain()
           } catch (e: Exception) {
               throw DataSourceException.MappingError("Failed to map user progress", e)
           }
       }
    
    suspend fun insertOrUpdateUserProgress(progress: UserProgress) {
        try {
            val dbModel = UserProgressDbModel(
                userId = progress.userId,
                completedLessons = progress.completedLessons.joinToString(",")
            )
            progressDao.insertOrUpdateUserProgress(dbModel)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to insert user progress", e)
        }
    }
    
    // Lesson Progress
    fun getLessonProgress(userId: String, lessonId: String): Flow<LessonProgress?> =
        progressDao.getLessonProgress(userId, lessonId).map { progress ->
            try {
                progress?.toDomain()
            } catch (e: Exception) {
                throw DataSourceException.MappingError("Failed to map lesson progress", e)
            }
        }
    
    fun getAllLessonsProgress(userId: String): Flow<List<LessonProgress>> =
        progressDao.getAllLessonsProgress(userId).map { lessonsProgressDb ->
            lessonsProgressDb.map { progress ->
                try {
                    progress.toDomain()
                } catch (e: Exception) {
                    throw DataSourceException.MappingError("Failed to map lesson progress", e)
                }
            }
        }
    
    suspend fun insertOrUpdateLessonProgress(userId: String, progress: LessonProgress) {
        try {
            val dbModel = LessonProgressDbModel(
                userId = userId,
                lessonId = progress.lessonId,
                completedTests = progress.completedTests.joinToString(",")
            )
            progressDao.insertOrUpdateLessonProgress(dbModel)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to insert lesson progress", e)
        }
    }
    
    suspend fun resetLessonProgress(userId: String, lessonId: String) {
        try {
            progressDao.resetLessonProgress(userId, lessonId)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to reset lesson progress", e)
        }
    }

    
    // Test Progress
    fun getTestProgress(userId: String, lessonId: String, testId: String): Flow<TestProgress?> =
        progressDao.getTestProgress(userId, lessonId, testId).map { progress ->
            try {
                progress?.toDomain()
            } catch (e: Exception) {
                throw DataSourceException.MappingError("Failed to map test progress", e)
            }
        }
    
    fun getAllTestsProgress(userId: String, lessonId: String): Flow<List<TestProgress>> =
        progressDao.getAllTestsProgress(userId, lessonId).map { testsProgressDb ->
            testsProgressDb.map { progress ->
                try {
                    progress.toDomain()
                } catch (e: Exception) {
                    throw DataSourceException.MappingError("Failed to map test progress", e)
                }
            }
        }
    
    suspend fun insertOrUpdateTestProgress(
        userId: String,
        lessonId: String,
        testId: String,
        progress: TestProgress
    ) {
        try {
            val dbModel = TestProgressDbModel(
                userId = userId,
                lessonId = lessonId,
                testId = testId,
                completedQuestions = progress.completedQuestions.joinToString(","),
                pendingQuestions = progress.pendingQuestions.joinToString(",")
            )
            progressDao.insertOrUpdateTestProgress(dbModel)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to insert test progress", e)
        }
    }
    
    suspend fun resetTestProgress(userId: String, lessonId: String, testId: String) {
        try {
            progressDao.resetTestProgress(userId, lessonId, testId)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to reset test progress", e)
        }
    }

    suspend fun markTestAsCompleted(userId: String, lessonId: String, testId: String) {
        try {
            val currentLessonProgress = getLessonProgress(userId, lessonId).first()

            val completedTests = currentLessonProgress?.completedTests?.toMutableSet()
                ?: mutableSetOf()
            completedTests.add(testId)

            val updatedProgress = LessonProgress(
                lessonId = lessonId,
                completedTests = completedTests.toSet()
            )

            insertOrUpdateLessonProgress(userId, updatedProgress)
        } catch (e: DataSourceException) {
            throw e
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to mark test as completed", e)
        }
    }

    suspend fun markLessonAsCompleted(userId: String, lessonId: String) {
        try {
            val currentUserProgress = getUserProgress(userId).first()

            val completedLessons = currentUserProgress?.completedLessons?.toMutableSet()
                ?: mutableSetOf()
            completedLessons.add(lessonId)

            val updatedProgress = UserProgress(
                userId = userId,
                completedLessons = completedLessons.toSet()
            )

            insertOrUpdateUserProgress(updatedProgress)
        } catch (e: DataSourceException) {
            throw e
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to mark lesson as completed", e)
        }
    }
}