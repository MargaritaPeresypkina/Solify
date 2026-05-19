package com.example.solify.data.repositories

import com.example.solify.data.local.data_sources.ProgressLocalDataSource
import com.example.solify.data.remote.firebase.data_source.ProgressRemoteDataSource
import com.example.solify.data.remote.firebase.dto.LessonProgressDto
import com.example.solify.data.remote.firebase.dto.TestProgressDto
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.TestProgress
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

    override fun getLessonProgress(userId: String, lessonId: String): Flow<LessonProgress?> {
        return flow {
            val cached = localDataSource.getLessonProgress(userId, lessonId).first()
            if (cached != null) emit(cached)

            val remote = remoteDataSource.getLessonProgress(userId, lessonId).getOrNull()
            if (remote != null) {
                val progress = LessonProgress(
                    lessonId = remote.lessonId,
                    completedTests = remote.completedTests.toSet(),
                    pendingTests = remote.pendingTests.toList()
                )
                localDataSource.insertOrUpdateLessonProgress(userId, progress)
                emit(progress)
            }
        }
    }

    override fun getAllLessonsProgress(userId: String): Flow<List<LessonProgress>> {
        return flow {
            val remote = remoteDataSource.getAllLessonsProgress(userId).getOrNull() ?: emptyList()
            val progresses = remote.map { dto ->
                LessonProgress(
                    lessonId = dto.lessonId,
                    completedTests = dto.completedTests.toSet(),
                    pendingTests = dto.pendingTests.toList()
                )
            }
            if (progresses.isNotEmpty()) {
                progresses.forEach { localDataSource.insertOrUpdateLessonProgress(userId, it) }
            }
            emit(progresses)
        }
    }

    override suspend fun saveLessonProgress(userId: String, progress: LessonProgress) {
        val dto = LessonProgressDto(
            lessonId = progress.lessonId,
            completedTests = progress.completedTests.toList(),
            pendingTests = progress.pendingTests.toList()
        )
        remoteDataSource.updateLessonProgress(userId, dto)
        localDataSource.insertOrUpdateLessonProgress(userId, progress)
    }

    override suspend fun clearLessonProgress(userId: String, lessonId: String) {
        localDataSource.resetLessonProgress(userId, lessonId)
        val emptyProgress = LessonProgressDto(lessonId = lessonId)
        remoteDataSource.updateLessonProgress(userId, emptyProgress)
    }

    override fun getTestProgress(userId: String, testId: String): Flow<TestProgress?> {
        return flow {
            val cached = localDataSource.getTestProgress(userId, testId).first()
            if (cached != null) emit(cached)

            val remote = remoteDataSource.getTestProgress(userId, testId).getOrNull()
            if (remote != null) {
                val progress = TestProgress(
                    testId = remote.testId,
                    completedQuestions = remote.completedQuestions.toSet(),
                    pendingQuestions = remote.pendingQuestions
                )
                localDataSource.insertOrUpdateTestProgress(userId, testId, progress)
                emit(progress)
            }
        }
    }

    override fun getAllTestsProgress(userId: String): Flow<List<TestProgress>> =
        localDataSource.getAllTestsProgress(userId)

    override suspend fun syncTestsProgress(userId: String) {
        syncAllTestsProgressFromRemote(userId)
    }

    override suspend fun saveTestProgress(userId: String, progress: TestProgress) {
        val dto = TestProgressDto(
            testId = progress.testId,
            completedQuestions = progress.completedQuestions.toList(),
            pendingQuestions = progress.pendingQuestions
        )
        remoteDataSource.updateTestProgress(userId, dto)
        localDataSource.insertOrUpdateTestProgress(userId, progress.testId, progress)
    }

    override suspend fun clearTestProgress(userId: String, testId: String) {
        localDataSource.resetTestProgress(userId, testId)
        val emptyProgress = TestProgressDto(testId = testId)
        remoteDataSource.updateTestProgress(userId, emptyProgress)
    }

    override suspend fun completeQuestion(
        userId: String,
        lessonId: String,
        testId: String,
        questionId: String
    ) {
        val testProgress = getTestProgress(userId, testId).first() ?: return

        val updatedCompleted = testProgress.completedQuestions.toMutableSet()
        updatedCompleted.add(questionId)

        val updatedPending = testProgress.pendingQuestions.toMutableList()
        updatedPending.remove(questionId)

        val updatedProgress = testProgress.copy(
            completedQuestions = updatedCompleted,
            pendingQuestions = updatedPending
        )

        saveTestProgress(userId, updatedProgress)

        if (updatedPending.isEmpty()) {
            completeTest(userId, lessonId, testId)
        }
    }

    override suspend fun completeTest(userId: String, lessonId: String, testId: String) {
        val lessonProgress = getLessonProgress(userId, lessonId).first() ?: return

        val updatedCompleted = lessonProgress.completedTests.toMutableSet()
        updatedCompleted.add(testId)

        val updatedPending = lessonProgress.pendingTests.toMutableSet()
        val newPending = updatedPending.toMutableSet()
        newPending.remove(testId)

        val updatedProgress = lessonProgress.copy(
            completedTests = updatedCompleted,
            pendingTests = newPending.toList()
        )

        saveLessonProgress(userId, updatedProgress)
    }

    override suspend fun getNextPendingTest(userId: String, lessonId: String): String? {
        val lessonProgress = getLessonProgress(userId, lessonId).first()
        return lessonProgress?.activePendingTests?.firstOrNull()
    }

    private suspend fun syncAllLessonsProgressFromRemote(userId: String) = withContext(Dispatchers.IO) {
        val remote = remoteDataSource.getAllLessonsProgress(userId).getOrNull() ?: return@withContext
        remote.forEach { dto ->
            val remoteProgress = dto.toDomain().normalize()
            val localProgress = localDataSource.getLessonProgress(userId, remoteProgress.lessonId).first()
            val merged = mergeLessonProgress(localProgress, remoteProgress).normalize()
            localDataSource.insertOrUpdateLessonProgress(userId, merged)
        }
    }

    private suspend fun syncAllTestsProgressFromRemote(userId: String) = withContext(Dispatchers.IO) {
        val remote = remoteDataSource.getAllTestsProgress(userId).getOrNull() ?: return@withContext
        remote.forEach { dto ->
            val remoteProgress = TestProgress(
                testId = dto.testId,
                completedQuestions = dto.completedQuestions.toSet(),
                pendingQuestions = dto.pendingQuestions
            )
            val localProgress = localDataSource.getTestProgress(userId, dto.testId).first()
            val merged = mergeTestProgress(localProgress, remoteProgress)
            localDataSource.insertOrUpdateTestProgress(userId, dto.testId, merged)
        }
    }
}