package com.example.solify.presentation.screens.trainings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.R
import com.example.solify.domain.entities.training.Training
import com.example.solify.domain.entities.training.TrainingCategory
import com.example.solify.domain.repositories.TrainingRepository
import com.example.solify.domain.usecases.trainings.SyncTrainingsUseCase
import com.example.solify.domain.usecases.user.GetUserBadgeUseCase
import com.example.solify.domain.usecases.user.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getUserBadgeUseCase: GetUserBadgeUseCase,
    private val syncTrainingsUseCase: SyncTrainingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingsUiState())
    val uiState: StateFlow<TrainingsUiState> = _uiState.asStateFlow()

    private var hasSyncedOnce = false

    init {
        observeCurrentUserUseCase()
            .filterNotNull()
            .distinctUntilChanged { old, new -> old.id == new.id }
            .onEach { user ->
                _uiState.update { it.copy(userAvatarUrl = user.avatarUrl) }
                refreshBadge(user.id)
                if (!hasSyncedOnce) {
                    hasSyncedOnce = true
                    syncTrainingsInBackground()
                }
            }
            .launchIn(viewModelScope)

        trainingRepository.observeAllTrainings()
            .onEach { trainings -> updateTrainings(trainings) }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load trainings"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun syncTrainingsInBackground() {
        viewModelScope.launch {
            syncTrainingsUseCase().onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to sync trainings"
                    )
                }
            }
        }
    }

    private fun refreshBadge(userId: String) {
        viewModelScope.launch {
            getUserBadgeUseCase(userId).let { result ->
                _uiState.update {
                    it.copy(
                        userBadgeRes = result.badgeRes,
                        userLevel = result.levelName,
                        isHeaderReady = true
                    )
                }
            }
        }
    }

    private fun updateTrainings(trainings: List<Training>) {
        val earTrainings = trainings
            .filter { it.category == TrainingCategory.EAR }
            .sortedBy { it.title }
            .map { it.toTrainingItemUi() }

        val rhythmTrainings = trainings
            .filter { it.category == TrainingCategory.RHYTHM }
            .sortedBy { it.title }
            .map { it.toTrainingItemUi() }

        _uiState.update { previousState ->
            previousState.copy(
                isLoading = false,
                error = null,
                hasLoadedOnce = true,
                earTrainings = earTrainings,
                rhythmTrainings = rhythmTrainings
            )
        }
    }

    private fun Training.toTrainingItemUi(): TrainingItemUi {
        return TrainingItemUi(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl
        )
    }
}

data class TrainingsUiState(
    val isLoading: Boolean = true,
    val hasLoadedOnce: Boolean = false,
    val isHeaderReady: Boolean = false,
    val error: String? = null,
    val earTrainings: List<TrainingItemUi> = emptyList(),
    val rhythmTrainings: List<TrainingItemUi> = emptyList(),
    val userAvatarUrl: String? = null,
    val userBadgeRes: Int = R.drawable.none_medal,
    val userLevel: String = "Let's try"
)

data class TrainingItemUi(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?
)
