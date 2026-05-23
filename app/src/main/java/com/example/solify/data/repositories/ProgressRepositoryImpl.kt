package com.example.solify.data.repositories

import com.example.solify.data.local.data_sources.ProgressLocalDataSource
import com.example.solify.data.remote.firebase.data_source.ProgressRemoteDataSource
import com.example.solify.data.remote.firebase.dto.ExerciseProgressDto
import com.example.solify.data.remote.firebase.dto.LessonProgressDto
import com.example.solify.data.remote.firebase.dto.TestProgressDto
import com.example.solify.domain.entities.progress.ExerciseProgress
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.entities.progress.withDerivedStatus
import com.example.solify.domain.repositories.ProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import com.example.solify.data.local.mappers.toDomain as exerciseProgressDtoToDomain
import com.example.solify.data.remote.firebase.mappers.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val localDataSource: ProgressLocalDataSource,
    private val remoteDataSource: ProgressRemoteDataSource
) : ProgressRepository {
    override fun getLessonProgress(userId: String, lessonId: String): Flow<LessonProgress?> =
        localDataSource.getLessonProgress(userId, lessonId)

    override fun getAllLessonsProgress(userId: String): Flow<List<LessonProgress>> =
        localDataSource.getAllLessonsProgress(userId)

    override suspend fun syncLessonsProgress(userId: String) {
        syncAllLessonsProgressFromRemote(userId)
    }

    override suspend fun saveLessonProgress(userId: String, progress: LessonProgress) {
        val normalized = progress.normalize()
        val dto = LessonProgressDto(
            lessonId = normalized.lessonId,
            completedTests = normalized.completedTests.toList(),
            pendingTests = normalized.pendingTests
        )
        remoteDataSource.updateLessonProgress(userId, dto)
        localDataSource.insertOrUpdateLessonProgress(userId, normalized)
    }

    override suspend fun clearLessonProgress(userId: String, lessonId: String) {
        localDataSource.resetLessonProgress(userId, lessonId)
        val emptyProgress = LessonProgressDto(lessonId = lessonId)
        remoteDataSource.updateLessonProgress(userId, emptyProgress)
    }

    override fun observeTestProgress(userId: String, testId: String): Flow<TestProgress?> =
        localDataSource.getTestProgress(userId, testId)

    override suspend fun getCurrentTestProgress(userId: String, testId: String): TestProgress? {
        val local = localDataSource.getTestProgress(userId, testId).first()
        if (local != null && !isTestProgressEmpty(local)) return local

        val remote = remoteDataSource.getTestProgress(userId, testId).getOrNull() ?: return local
        val remoteProgress = TestProgress(
            testId = remote.testId,
            completedQuestions = remote.completedQuestions.toSet(),
            pendingQuestions = remote.pendingQuestions
        )
        val merged = mergeTestProgress(local, remoteProgress)
        if (!isTestProgressEmpty(merged)) {
            localDataSource.insertOrUpdateTestProgress(userId, testId, merged)
        }
        return merged.takeUnless { isTestProgressEmpty(it) }
    }

    override suspend fun isTestCompleted(userId: String, lessonId: String, testId: String): Boolean {
        val lessonProgress = getLessonProgress(userId, lessonId).first()
        return lessonProgress?.completedTests?.contains(testId) == true
    }

    override fun getTestProgress(userId: String, testId: String): Flow<TestProgress?> {
        return flow {
            emit(getCurrentTestProgress(userId, testId))
        }
    }

    override fun getAllTestsProgress(userId: String): Flow<List<TestProgress>> =
        localDataSource.getAllTestsProgress(userId)

    override fun observeTestsProgressForLesson(
        userId: String,
        lessonId: String
    ): Flow<List<TestProgress>> =
        localDataSource.observeTestsProgressForLesson(userId, lessonId)

    override suspend fun syncTestsProgress(userId: String) {
        syncAllTestsProgressFromRemote(userId)
    }

    override suspend fun saveTestProgress(userId: String, progress: TestProgress) {
        val normalized = progress.withDerivedStatus()
        localDataSource.insertOrUpdateTestProgress(userId, normalized.testId, normalized)
        val dto = TestProgressDto(
            testId = normalized.testId,
            completedQuestions = normalized.completedQuestions.toList(),
            pendingQuestions = normalized.pendingQuestions,
            status = normalized.status.name
        )
        remoteDataSource.updateTestProgress(userId, dto)
    }

    override suspend fun clearTestProgress(userId: String, testId: String) {
        localDataSource.resetTestProgress(userId, testId)
        val emptyProgress = TestProgressDto(testId = testId)
        remoteDataSource.updateTestProgress(userId, emptyProgress)
    }

    override fun observeExerciseProgress(userId: String, trainerId: String): Flow<ExerciseProgress?> =
        localDataSource.getExerciseProgress(userId, trainerId)

    override suspend fun getCurrentExerciseProgress(userId: String, trainerId: String): ExerciseProgress? {
        val local = localDataSource.getExerciseProgress(userId, trainerId).first()
        if (local != null && !isExerciseProgressEmpty(local)) return local

        val remote = remoteDataSource.getExerciseProgress(userId, trainerId).getOrNull() ?: return local
        val remoteProgress = remote.exerciseProgressDtoToDomain()
        val merged = mergeExerciseProgress(local, remoteProgress)
        if (!isExerciseProgressEmpty(merged)) {
            localDataSource.insertOrUpdateExerciseProgress(userId, trainerId, merged)
        }
        return merged.takeUnless { isExerciseProgressEmpty(it) }
    }

    override suspend fun saveExerciseProgress(userId: String, progress: ExerciseProgress) {
        val normalized = progress.withDerivedStatus()
        localDataSource.insertOrUpdateExerciseProgress(userId, normalized.trainerId, normalized)
        val dto = ExerciseProgressDto(
            trainerId = normalized.trainerId,
            completedExercises = normalized.completedExercises.toList(),
            pendingExercises = normalized.pendingExercises,
            status = normalized.status.name
        )
        remoteDataSource.updateExerciseProgress(userId, dto)
    }

    override suspend fun clearExerciseProgress(userId: String, trainerId: String) {
        localDataSource.resetExerciseProgress(userId, trainerId)
        val emptyProgress = ExerciseProgressDto(trainerId = trainerId)
        remoteDataSource.updateExerciseProgress(userId, emptyProgress)
    }

    override suspend fun completeQuestion(
        userId: String,
        lessonId: String,
        testId: String,
        questionId: String
    ) {
        val testProgress = getCurrentTestProgress(userId, testId) ?: return

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
        val lessonProgress = localDataSource.getLessonProgress(userId, lessonId).first() ?: return
        val updatedCompleted = lessonProgress.completedTests.toMutableSet()
        updatedCompleted.add(testId)

        val newPending = lessonProgress.pendingTests
            .filter { it.isNotBlank() && it != testId }

        val updatedProgress = lessonProgress.copy(
            completedTests = updatedCompleted,
            pendingTests = newPending
        ).normalize()

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

    private fun mergeLessonProgress(
        local: LessonProgress?,
        remote: LessonProgress
    ): LessonProgress {
        val normalizedRemote = remote.normalize()
        if (local == null) return normalizedRemote
        if (isLessonProgressEmpty(normalizedRemote) && !isLessonProgressEmpty(local)) return local.normalize()
        if (isLessonProgressEmpty(local)) return normalizedRemote

        return LessonProgress(
            lessonId = normalizedRemote.lessonId,
            completedTests = local.completedTests + normalizedRemote.completedTests,
            pendingTests = if (local.completedTests.size >= normalizedRemote.completedTests.size) {
                local.pendingTests
            } else {
                normalizedRemote.pendingTests
            }
        ).normalize()
    }

    private fun mergeTestProgress(
        local: TestProgress?,
        remote: TestProgress
    ): TestProgress {
        if (local == null) return remote
        if (isTestProgressEmpty(remote) && !isTestProgressEmpty(local)) return local
        if (isTestProgressEmpty(local)) return remote

        val mergedCompleted = local.completedQuestions + remote.completedQuestions
        val mergedPending = (local.pendingQuestions + remote.pendingQuestions)
            .distinct()
            .filter { questionId -> questionId !in mergedCompleted }

        return TestProgress(
            testId = remote.testId,
            lessonId = local.lessonId ?: remote.lessonId,
            completedQuestions = mergedCompleted,
            pendingQuestions = when {
                mergedPending.isNotEmpty() -> mergedPending
                local.completedQuestions.size >= remote.completedQuestions.size -> local.pendingQuestions
                else -> remote.pendingQuestions
            }
        ).withDerivedStatus()
    }

    private fun isLessonProgressEmpty(progress: LessonProgress): Boolean = progress.completedTests.isEmpty() && progress.activePendingTests.isEmpty()

    private fun isTestProgressEmpty(progress: TestProgress): Boolean =
        progress.completedQuestions.isEmpty() && progress.pendingQuestions.isEmpty()

    private fun mergeExerciseProgress(
        local: ExerciseProgress?,
        remote: ExerciseProgress
    ): ExerciseProgress {
        if (local == null) return remote
        if (isExerciseProgressEmpty(remote) && !isExerciseProgressEmpty(local)) return local
        if (isExerciseProgressEmpty(local)) return remote

        val mergedCompleted = local.completedExercises + remote.completedExercises
        val mergedPending = (local.pendingExercises + remote.pendingExercises)
            .distinct()
            .filter { exerciseId -> exerciseId !in mergedCompleted }

        return ExerciseProgress(
            trainerId = remote.trainerId,
            completedExercises = mergedCompleted,
            pendingExercises = when {
                mergedPending.isNotEmpty() -> mergedPending
                local.completedExercises.size >= remote.completedExercises.size -> local.pendingExercises
                else -> remote.pendingExercises
            }
        ).withDerivedStatus()
    }

    private fun isExerciseProgressEmpty(progress: ExerciseProgress): Boolean =
        progress.completedExercises.isEmpty() && progress.pendingExercises.isEmpty()
}