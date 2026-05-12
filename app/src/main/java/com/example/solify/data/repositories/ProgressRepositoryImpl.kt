package com.example.solify.data.repositories

import android.util.Log
import com.example.solify.data.local.data_sources.ProgressLocalDataSource
import com.example.solify.data.remote.firebase.data_source.ProgressRemoteDataSource
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.entities.progress.UserProgress
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val localDataSource: ProgressLocalDataSource,
    private val remoteDataSource: ProgressRemoteDataSource
) : ProgressRepository {

    override fun getUserProgress(userId: String): Flow<UserProgress?> {
        return flow {
            val remoteProgress = remoteDataSource.getUserProgress(userId).getOrNull()
            Log.d("LessonsDebug", "remoteProgress ${remoteProgress?.completedLessons}")

            if (remoteProgress != null) {
                val userProgress = UserProgress(
                    userId = userId,
                    completedLessons = remoteProgress.completedLessons.toSet()
                )
                localDataSource.insertOrUpdateUserProgress(userProgress)
                emit(userProgress)
            } else {
                val localProgress = localDataSource.getUserProgress(userId).first()
                if (localProgress != null) {
                    emit(localProgress)
                } else {
                    val emptyProgress = UserProgress(
                        userId = userId,
                        completedLessons = emptySet()
                    )
                    emit(emptyProgress)
                }
            }
        }
    }

    override fun getLessonProgress(userId: String, lessonId: String): Flow<LessonProgress?> {
        return localDataSource.getLessonProgress(userId, lessonId)
    }

    override fun getAllLessonsProgress(userId: String): Flow<List<LessonProgress>> {
        return localDataSource.getAllLessonsProgress(userId)
    }

    override fun getTestProgress(
        userId: String,
        lessonId: String,
        testId: String
    ): Flow<TestProgress?> {
        return localDataSource.getTestProgress(userId, lessonId, testId)
    }

    override fun getAllTestsProgress(
        userId: String,
        lessonId: String
    ): Flow<List<TestProgress>> {
        return localDataSource.getAllTestsProgress(userId, lessonId)
    }

    override suspend fun saveTestProgress(
        userId: String,
        lessonId: String,
        testId: String,
        progress: TestProgress
    ) {
        localDataSource.insertOrUpdateTestProgress(userId, lessonId, testId, progress)
    }

    override suspend fun saveLessonProgress(
        userId: String,
        lessonId: String,
        progress: LessonProgress
    ) {
        localDataSource.insertOrUpdateLessonProgress(userId, progress)
    }

    override suspend fun saveUserProgress(progress: UserProgress) {
        remoteDataSource.updateUserProgress(
            userId = progress.userId,
            completedLessons = progress.completedLessons.toList()
        )
        localDataSource.insertOrUpdateUserProgress(progress)
    }

    override suspend fun clearTestProgress(
        userId: String,
        lessonId: String,
        testId: String
    ) {
        localDataSource.resetTestProgress(userId, lessonId, testId)
    }

    override suspend fun clearLessonProgress(
        userId: String,
        lessonId: String
    ) {
        localDataSource.resetLessonProgress(userId, lessonId)
    }

    override suspend fun markTestAsCompleted(
        userId: String,
        lessonId: String,
        testId: String
    ) {
        localDataSource.markTestAsCompleted(userId, lessonId, testId)
    }

    override suspend fun markLessonAsCompleted(
        userId: String,
        lessonId: String
    ) {
        val remoteProgress = remoteDataSource.getUserProgress(userId).getOrNull()
        val completedLessons = remoteProgress?.completedLessons?.toMutableSet() ?: mutableSetOf()
        completedLessons.add(lessonId)

        val updatedProgress = UserProgress(
            userId = userId,
            completedLessons = completedLessons
        )

        saveUserProgress(updatedProgress)
    }
}