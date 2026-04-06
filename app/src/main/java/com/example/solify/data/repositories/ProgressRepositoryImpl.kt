package com.example.solify.data.repositories

import com.example.solify.data.dao.ProgressDao
import com.example.solify.data.db_models.LessonProgressDbModel
import com.example.solify.data.db_models.TestProgressDbModel
import com.example.solify.data.db_models.UserProgressDbModel
import com.example.solify.data.mappers.toDomain
import com.example.solify.data.mappers.toSet
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.entities.progress.UserProgress
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao
) : ProgressRepository {

    override fun getUserProgress(userId: String): Flow<UserProgress?> {
        return progressDao.getUserProgress(userId).map { it?.toDomain() }
    }

    override fun getLessonProgress(userId: String, lessonId: String): Flow<LessonProgress?> {
        return progressDao.getLessonProgress(userId, lessonId).map { lessonProgressDb ->
            lessonProgressDb?.toDomain()
        }
    }

    override fun getAllLessonsProgress(userId: String): Flow<List<LessonProgress>> {
        return progressDao.getAllLessonsProgress(userId).map { lessonsProgressDb ->
            lessonsProgressDb.map { it.toDomain() }
        }
    }

    override fun getTestProgress(
        userId: String,
        lessonId: String,
        testId: String
    ): Flow<TestProgress?> {
        return progressDao.getTestProgress(userId, lessonId, testId).map { testProgressDb ->
            testProgressDb?.toDomain()
        }
    }

    override fun getAllTestsProgress(
        userId: String,
        lessonId: String
    ): Flow<List<TestProgress>> {
        return progressDao.getAllTestsProgress(userId, lessonId).map { testsProgressDb ->
            testsProgressDb.map { it.toDomain() }
        }
    }

    override suspend fun saveTestProgress(
        userId: String,
        lessonId: String,
        testId: String,
        progress: TestProgress
    ) {
        progressDao.insertOrUpdateTestProgress(
            TestProgressDbModel(
                userId = userId,
                lessonId = lessonId,
                testId = testId,
                completedQuestions = progress.completedQuestions.joinToString(","),
                pendingQuestions = progress.pendingQuestions.joinToString(",")
            )
        )
    }

    override suspend fun saveLessonProgress(
        userId: String,
        lessonId: String,
        progress: LessonProgress
    ) {
        progressDao.insertOrUpdateLessonProgress(
            LessonProgressDbModel(
                userId = userId,
                lessonId = lessonId,
                completedTests = progress.completedTests.joinToString(",")
            )
        )
    }

    override suspend fun saveUserProgress(progress: UserProgress) {
        progressDao.insertOrUpdateUserProgress(
            UserProgressDbModel(
                userId = progress.userId,
                completedLessons = progress.completedLessons.joinToString(",")
            )
        )
    }

    override suspend fun clearTestProgress(
        userId: String,
        lessonId: String,
        testId: String
    ) {
        progressDao.resetTestProgress(userId, lessonId, testId)
    }

    override suspend fun clearLessonProgress(
        userId: String,
        lessonId: String
    ) {
        progressDao.resetLessonProgress(userId, lessonId)
    }

    override suspend fun markTestAsCompleted(
        userId: String,
        lessonId: String,
        testId: String
    ) {
        val currentLessonProgress = progressDao.getLessonProgress(userId, lessonId).first()

        val completedTests = currentLessonProgress?.completedTests?.toSet()?.toMutableSet()
            ?: mutableSetOf()
        completedTests.add(testId)

        progressDao.insertOrUpdateLessonProgress(
            LessonProgressDbModel(
                userId = userId,
                lessonId = lessonId,
                completedTests = completedTests.joinToString(",")
            )
        )
    }

    override suspend fun markLessonAsCompleted(
        userId: String,
        lessonId: String
    ) {
        val currentUserProgress = progressDao.getUserProgress(userId).first()

        val completedLessons = currentUserProgress?.completedLessons?.toSet()?.toMutableSet()
            ?: mutableSetOf()
        completedLessons.add(lessonId)

        progressDao.insertOrUpdateUserProgress(
            UserProgressDbModel(
                userId = userId,
                completedLessons = completedLessons.joinToString(",")
            )
        )
    }
}