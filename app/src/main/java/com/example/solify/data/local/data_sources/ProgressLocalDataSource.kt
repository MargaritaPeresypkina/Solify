package com.example.solify.data.local.data_sources

import com.example.solify.data.local.dao.ProgressDao
import com.example.solify.data.local.db_models.DailyActivityDbModel
import com.example.solify.data.local.db_models.ExerciseProgressDbModel
import com.example.solify.data.local.db_models.LessonProgressDbModel
import com.example.solify.data.local.db_models.TestProgressDbModel
import com.example.solify.data.local.db_models.UserProgressDbModel
import com.example.solify.data.local.mappers.toDbModel
import com.example.solify.data.local.mappers.toDomain
import com.example.solify.domain.entities.progress.DailyActivity
import com.example.solify.domain.entities.progress.ExerciseProgress
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
            progressDao.insertOrUpdateUserProgress(progress.toDbModel())
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to insert user progress", e)
        }
    }

    suspend fun incrementCompletedExercisesCount(userId: String) {
        try {
            val current = getUserProgress(userId).first()
            val updated = UserProgress(
                userId = userId,
                completedLessons = current?.completedLessons.orEmpty(),
                completedExercisesCount = (current?.completedExercisesCount ?: 0) + 1
            )
            insertOrUpdateUserProgress(updated)
        } catch (e: DataSourceException) {
            throw e
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to increment completed exercises", e)
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
    

    suspend fun resetLessonProgress(userId: String, lessonId: String) {
        try {
            progressDao.resetLessonProgress(userId, lessonId)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to reset lesson progress", e)
        }
    }

    
    // Test Progress
    fun getTestProgress(userId: String, testId: String): Flow<TestProgress?> =
        progressDao.getTestProgress(userId, testId).map { progress ->
            try {
                progress?.toDomain()
            } catch (e: Exception) {
                throw DataSourceException.MappingError("Failed to map test progress", e)
            }
        }

    fun getAllTestsProgress(userId: String): Flow<List<TestProgress>> =
        progressDao.getAllTestsProgress(userId).map { testsProgressDb ->
            testsProgressDb.map { progress ->
                try {
                    progress.toDomain()
                } catch (e: Exception) {
                    throw DataSourceException.MappingError("Failed to map test progress", e)
                }
            }
        }

    fun observeTestsProgressForLesson(
        userId: String,
        lessonId: String
    ): Flow<List<TestProgress>> =
        progressDao.observeTestsProgressForLesson(userId, lessonId).map { testsProgressDb ->
            testsProgressDb.map { progress ->
                try {
                    progress.toDomain()
                } catch (e: Exception) {
                    throw DataSourceException.MappingError("Failed to map test progress", e)
                }
            }
        }

    suspend fun insertOrUpdateLessonProgress(userId: String, progress: LessonProgress) {
        try {
            val userExists = progressDao.hasUser(userId)
            val lessonExists = progressDao.hasLesson(progress.lessonId)
            if (!userExists || !lessonExists) {
                throw DataSourceException.DatabaseError(
                    "FK parents missing: userExists=$userExists lessonExists=$lessonExists",
                    IllegalStateException("FOREIGN KEY parents missing")
                )
            }
            val dbModel = LessonProgressDbModel(
                userId = userId,
                lessonId = progress.lessonId,
                completedTests = progress.completedTests.toList(),
                pendingTests = progress.pendingTests.toList()
            )
            progressDao.insertOrUpdateLessonProgress(dbModel)
        } catch (e: Exception) {
            e.printStackTrace()
            throw DataSourceException.DatabaseError("Failed to insert lesson progress: ${e.message}", e)
        }
    }

    suspend fun insertOrUpdateTestProgress(
        userId: String,
        testId: String,
        progress: TestProgress
    ) {
        try {
            val existing = progressDao.getTestProgress(userId, testId).first()
            val dbModel = progress.toDbModel(
                userId = userId,
                existingId = existing?.id ?: 0
            )
            progressDao.insertOrUpdateTestProgress(dbModel)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to insert test progress", e)
        }
    }

    suspend fun resetTestProgress(userId: String, testId: String) {
        try {
            progressDao.resetTestProgress(userId, testId)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to reset test progress", e)
        }
    }

    fun getExerciseProgress(userId: String, trainerId: String): Flow<ExerciseProgress?> =
        progressDao.getExerciseProgress(userId, trainerId).map { progress ->
            try {
                progress?.toDomain()
            } catch (e: Exception) {
                throw DataSourceException.MappingError("Failed to map exercise progress", e)
            }
        }

    suspend fun insertOrUpdateExerciseProgress(
        userId: String,
        trainerId: String,
        progress: ExerciseProgress
    ) {
        try {
            val existing = progressDao.getExerciseProgress(userId, trainerId).first()
            val dbModel = progress.toDbModel(
                userId = userId,
                existingId = existing?.id ?: 0
            )
            progressDao.insertOrUpdateExerciseProgress(dbModel)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to insert exercise progress", e)
        }
    }

    suspend fun resetExerciseProgress(userId: String, trainerId: String) {
        try {
            progressDao.resetExerciseProgress(userId, trainerId)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to reset exercise progress", e)
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

    fun observeDailyActivitySince(userId: String, sinceDate: String): Flow<List<DailyActivity>> =
        progressDao.observeDailyActivitySince(userId, sinceDate).map { entries ->
            entries.map { it.toDailyActivity() }
        }

    suspend fun getDailyActivitySince(userId: String, sinceDate: String): List<DailyActivity> =
        progressDao.getDailyActivitySince(userId, sinceDate).map { it.toDailyActivity() }

    suspend fun incrementDailyActivity(userId: String, date: String) {
        try {
            val userExists = progressDao.hasUser(userId)
            if (!userExists) {
                throw DataSourceException.DatabaseError(
                    "FK user missing for daily_activity",
                    IllegalStateException("user row missing")
                )
            }
            val existing = progressDao.getDailyActivity(userId, date)
            val updated = DailyActivityDbModel(
                userId = userId,
                date = date,
                completedTestsCount = (existing?.completedTestsCount ?: 0) + 1
            )
            progressDao.insertOrUpdateDailyActivity(updated)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to increment daily activity", e)
        }
    }

    suspend fun insertOrUpdateDailyActivities(userId: String, activities: List<DailyActivity>) {
        try {
            activities.forEach { activity ->
                progressDao.insertOrUpdateDailyActivity(
                    DailyActivityDbModel(
                        userId = userId,
                        date = activity.date,
                        completedTestsCount = activity.completedTestsCount
                    )
                )
            }
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to save daily activity", e)
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
                completedLessons = completedLessons.toSet(),
                completedExercisesCount = currentUserProgress?.completedExercisesCount ?: 0
            )

            insertOrUpdateUserProgress(updatedProgress)
        } catch (e: DataSourceException) {
            throw e
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to mark lesson as completed", e)
        }
    }

    private fun DailyActivityDbModel.toDailyActivity() = DailyActivity(
        date = date,
        completedTestsCount = completedTestsCount
    )
}