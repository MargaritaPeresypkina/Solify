package com.example.solify.domain.usecases.user

import com.example.solify.R
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.sync.UserDataSyncCoordinator
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetUserBadgeUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val lessonRepository: LessonRepository,
    private val userDataSyncCoordinator: UserDataSyncCoordinator
) {
    suspend operator fun invoke(userId: String): BadgeResult {
        return try {
            userDataSyncCoordinator.ensureSynced(userId)

            val lessonsProgress = progressRepository.getAllLessonsProgress(userId).first()
            val allTestIds = lessonRepository.observeTestIdsByLesson().first()
                .values
                .flatten()
                .toSet()

            val totalTests = allTestIds.size
            val completedTests = lessonsProgress
                .flatMap { it.completedTests }
                .filter { it in allTestIds }
                .toSet()
                .size

            val completionPercent = if (totalTests > 0) {
                completedTests * 100 / totalTests
            } else {
                0
            }

            when {
                completionPercent >= ADVANCED_THRESHOLD_PERCENT -> BadgeResult(
                    badgeRes = R.drawable.gold_medal__1_,
                    levelName = "Advanced"
                )
                completionPercent >= INTERMEDIATE_THRESHOLD_PERCENT -> BadgeResult(
                    badgeRes = R.drawable.silver_medal,
                    levelName = "Intermediate"
                )
                completionPercent >= BEGINNER_THRESHOLD_PERCENT -> BadgeResult(
                    badgeRes = R.drawable.bronze_medal_main,
                    levelName = "Beginner"
                )
                else -> BadgeResult(
                    badgeRes = R.drawable.none_medal,
                    levelName = "Let's try"
                )
            }
        } catch (e: Exception) {
            BadgeResult(
                badgeRes = R.drawable.none_medal,
                levelName = "Something went wrong"
            )
        }
    }

    private companion object {
        const val BEGINNER_THRESHOLD_PERCENT = 20
        const val INTERMEDIATE_THRESHOLD_PERCENT = 50
        const val ADVANCED_THRESHOLD_PERCENT = 80
    }
}

data class BadgeResult(
    val badgeRes: Int,
    val levelName: String
)
