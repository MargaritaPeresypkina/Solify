package com.example.solify.domain.usecases.progress

import com.example.solify.domain.entities.progress.TestsCompletionOverview
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.sync.UserDataSyncCoordinator
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CalculateTestsCompletionOverviewUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val lessonRepository: LessonRepository,
    private val userDataSyncCoordinator: UserDataSyncCoordinator
) {
    suspend operator fun invoke(userId: String): TestsCompletionOverview {
        userDataSyncCoordinator.ensureSynced(userId)

        val allTestIds = lessonRepository.fetchAllTestIdsFromRemote().getOrElse { emptySet() }
        val totalTestsFirebase = allTestIds.size

        val localTestIdSet = lessonRepository.observeTestIdsByLesson()
            .first()
            .values
            .flatten()
            .toSet()
        val totalTestsLocal = localTestIdSet.size

        val snapshot = progressRepository.fetchTestsCompletionSnapshot(userId, allTestIds)
        val remoteLessonCompletedIds = progressRepository.fetchRemoteCompletedTestIds(userId)
        val orphanCompleted = remoteLessonCompletedIds.count { it !in allTestIds }

        val percentFirebaseLesson = if (totalTestsFirebase > 0) {
            snapshot.completedFromLessonProgress * 100 / totalTestsFirebase
        } else {
            0
        }
        val percentLocalLesson = if (totalTestsLocal > 0) {
            snapshot.completedFromLocalLessonProgress * 100 / totalTestsLocal
        } else {
            0
        }

        return TestsCompletionOverview(
            percent = percentFirebaseLesson,
            completedTests = snapshot.completedFromLessonProgress,
            totalTests = totalTestsFirebase
        )
    }
}
