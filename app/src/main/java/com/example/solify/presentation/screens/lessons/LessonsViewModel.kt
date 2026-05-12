package com.example.solify.presentation.screens.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.R
import com.example.solify.domain.entities.lesson.Level
import com.example.solify.domain.entities.progress.Status
import com.example.solify.domain.usecases.lessons.GetAllLessonsWithStatusUseCase
import com.example.solify.domain.usecases.lessons.LessonWithStatus
import com.example.solify.domain.usecases.lessons.SyncLessonsUseCase
import com.example.solify.domain.usecases.user.GetCurrentUserUseCase
import com.example.solify.domain.usecases.user.GetUserBadgeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val getAllLessonsWithStatusUseCase: GetAllLessonsWithStatusUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserBadgeUseCase: GetUserBadgeUseCase,
    private val syncLessonsUseCase: SyncLessonsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonsUiState())
    val uiState: StateFlow<LessonsUiState> = _uiState.asStateFlow()

    init {
        loadData()
        syncLessons()
    }

    private fun syncLessons() {
        viewModelScope.launch {
            syncLessonsUseCase()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val userResult = getCurrentUserUseCase()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!
                loadUserBadge(user.id)
                loadLessonsWithStatus(user.id)
                _uiState.update {
                    it.copy(
                        userAvatarUrl = user.avatarUrl,
                        isLoading = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = userResult.exceptionOrNull()?.message ?: "Failed to load user"
                    )
                }
            }
        }
    }

    private fun loadUserBadge(userId: String) {
        viewModelScope.launch {
            getUserBadgeUseCase(userId).let { result ->
                _uiState.update {
                    it.copy(
                        userBadgeRes = result.badgeRes,
                        userLevel = result.levelName
                    )
                }
            }
        }
    }

    private fun loadLessonsWithStatus(userId: String) {
        getAllLessonsWithStatusUseCase(userId)
            .onEach { lessons ->
                val grouped = lessons.groupBy { it.level }

                _uiState.update { previousState ->
                    previousState.copy(
                        isLoading = false,
                        error = null,
                        beginnerLessons = grouped[Level.BEGINNER]
                            .orEmpty()
                            .sortedBy { lesson -> lesson.order }
                            .map { it.toLessonItemUi() },
                        intermediateLessons = grouped[Level.INTERMEDIATE]
                            .orEmpty()
                            .sortedBy { lesson -> lesson.order }
                            .map { it.toLessonItemUi() },
                        advancedLessons = grouped[Level.ADVANCED]
                            .orEmpty()
                            .sortedBy { lesson -> lesson.order }
                            .map { it.toLessonItemUi() }
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load lessons"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun LessonWithStatus.toLessonItemUi(): LessonItemUi {
        val iconRes = when (status) {
            Status.COMPLETED -> R.drawable.tick_done
            Status.IN_PROGRESS -> R.drawable.lightning_in_progress
            Status.NOT_STARTED -> R.drawable.lock_uncomplete
        }

        return LessonItemUi(
            id = id,
            title = title,
            description = description,
            level = level,
            status = status,
            iconRes = iconRes
        )
    }
}

data class LessonsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val beginnerLessons: List<LessonItemUi> = emptyList(),
    val intermediateLessons: List<LessonItemUi> = emptyList(),
    val advancedLessons: List<LessonItemUi> = emptyList(),
    val userAvatarUrl: String? = null,
    val userBadgeRes: Int = R.drawable.none_medal,
    val userLevel: String = "Let's try"
)

data class LessonItemUi(
    val id: String,
    val title: String,
    val description: String,
    val level: Level,
    val status: Status,
    val iconRes: Int
)