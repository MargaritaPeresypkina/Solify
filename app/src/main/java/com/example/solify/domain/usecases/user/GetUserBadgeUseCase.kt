package com.example.solify.domain.usecases.user

import com.example.solify.R
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.sync.UserDataSyncCoordinator
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetUserBadgeUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val userDataSyncCoordinator: UserDataSyncCoordinator
) {
    suspend operator fun invoke(userId: String): BadgeResult {
        return try {
            userDataSyncCoordinator.ensureSynced(userId)
            val completedCount = progressRepository.getAllLessonsProgress(userId).first()
                .count { lessonProgress -> lessonProgress.isLessonCompleted }

            when {
                completedCount >= 10 -> BadgeResult(
                    badgeRes = R.drawable.gold_medal__1_,
                    levelName = "Advanced"
                )
                completedCount >= 2 -> BadgeResult(
                    badgeRes = R.drawable.silver_medal,
                    levelName = "Intermediate"
                )
                completedCount >= 1 -> BadgeResult(
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
}

data class BadgeResult(
    val badgeRes: Int,
    val levelName: String
)