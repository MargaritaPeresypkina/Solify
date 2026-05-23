package com.example.solify.presentation.screens.trainers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.domain.entities.training.Trainer
import com.example.solify.domain.usecases.trainings.GetTrainingByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTrainingByIdUseCase: GetTrainingByIdUseCase
) : ViewModel() {

    private val trainingId: String = savedStateHandle.get<String>("training_id").orEmpty()

    private val _uiState = MutableStateFlow(TrainersUiState(trainingId = trainingId))
    val uiState: StateFlow<TrainersUiState> = _uiState.asStateFlow()

    init {
        loadTraining()
    }

    private fun loadTraining() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getTrainingByIdUseCase(trainingId).fold(
                onSuccess = { training ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoadedOnce = true,
                            error = null,
                            trainingTitle = training.title,
                            trainers = training.trainers
                                .sortedBy { it.order }
                                .map { trainer -> trainer.toUi() }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load training"
                        )
                    }
                }
            )
        }
    }
}

private fun Trainer.toUi() = TrainerItemUi(
    id = id,
    title = title,
    description = description
)

data class TrainersUiState(
    val trainingId: String = "",
    val trainingTitle: String = "",
    val isLoading: Boolean = true,
    val hasLoadedOnce: Boolean = false,
    val error: String? = null,
    val trainers: List<TrainerItemUi> = emptyList()
)

data class TrainerItemUi(
    val id: String,
    val title: String,
    val description: String
)
