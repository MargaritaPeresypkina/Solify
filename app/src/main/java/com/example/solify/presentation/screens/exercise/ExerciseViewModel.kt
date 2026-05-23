package com.example.solify.presentation.screens.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.domain.entities.training.Exercise
import com.example.solify.domain.entities.training.ExercisesAnswerOption
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.usecases.trainings.GetNextExerciseUseCase
import com.example.solify.domain.usecases.trainings.GetTrainerExercisesUseCase
import com.example.solify.domain.usecases.trainings.StartTrainerSessionUseCase
import com.example.solify.domain.usecases.trainings.SubmitExerciseAnswerUseCase
import com.example.solify.domain.usecases.user.ObserveCurrentUserUseCase
import com.example.solify.presentation.screens.test.ActionButtonColor
import com.example.solify.presentation.screens.test.AnswerOptionVisualState
import com.example.solify.presentation.screens.test.AnswerPhase
import com.example.solify.presentation.screens.test.TestAction
import com.example.solify.presentation.screens.theory.TheoryAudioController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val progressRepository: ProgressRepository,
    private val getTrainerExercisesUseCase: GetTrainerExercisesUseCase,
    private val startTrainerSessionUseCase: StartTrainerSessionUseCase,
    private val getNextExerciseUseCase: GetNextExerciseUseCase,
    private val submitExerciseAnswerUseCase: SubmitExerciseAnswerUseCase
) : ViewModel() {

    private val trainerId: String = savedStateHandle.get<String>("trainer_id").orEmpty()

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    private val audioController = TheoryAudioController()

    private var currentUserId: String? = null
    private var currentExercise: Exercise? = null
    private var totalExercises: Int = 0

    init {
        observeCurrentUserUseCase()
            .filterNotNull()
            .distinctUntilChanged { old, new -> old.id == new.id }
            .onEach { user ->
                currentUserId = user.id
                if (!_uiState.value.hasInitialized) {
                    initializeTrainerSession(user.id)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun initializeTrainerSession(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getTrainerExercisesUseCase(trainerId)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load trainer"
                        )
                    }
                    return@launch
                }
                .onSuccess { result ->
                    totalExercises = result.exercises.size
                    _uiState.update {
                        it.copy(
                            trainerTitle = result.trainerTitle,
                            totalExercises = totalExercises
                        )
                    }
                }

            startTrainerSessionUseCase(userId, trainerId).onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message)
                }
                return@launch
            }

            observeExerciseProgress(userId)
            loadCurrentExercise(userId)
        }
    }

    private fun observeExerciseProgress(userId: String) {
        progressRepository.observeExerciseProgress(userId, trainerId)
            .onEach { progress ->
                if (_uiState.value.isSessionCompleted) return@onEach

                val completed = progress?.completedExercises?.size ?: 0
                val total = totalExercises.takeIf { it > 0 }
                    ?: ((progress?.completedExercises?.size ?: 0) + (progress?.pendingExercises?.size ?: 0))
                _uiState.update {
                    it.copy(
                        completedExercises = completed,
                        totalExercises = total.coerceAtLeast(1),
                        hasInitialized = true
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun loadCurrentExercise(userId: String) {
        getNextExerciseUseCase(userId, trainerId)
            .onSuccess { exercise ->
                if (exercise == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No exercises available",
                            isSessionCompleted = true,
                            hasInitialized = true
                        )
                    }
                    return
                }

                currentExercise = exercise
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        hasInitialized = true,
                        exerciseText = exercise.text,
                        audioUrl = exercise.audio.takeIf { url -> url.isNotBlank() },
                        options = exercise.options.shuffled().map { option -> option.toUi() },
                        selectedOptionId = null,
                        answerPhase = AnswerPhase.SELECTING,
                        correctOptionId = null,
                        isAnswerCorrect = null,
                        isSessionCompleted = false,
                        actionButtonText = TestAction.CHECK.label
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load exercise"
                    )
                }
            }
    }

    fun onOptionSelected(optionId: String) {
        val state = _uiState.value
        if (state.answerPhase != AnswerPhase.SELECTING) return

        _uiState.update {
            it.copy(
                selectedOptionId = optionId,
                options = it.options.map { option ->
                    option.copy(
                        visualState = if (option.id == optionId) {
                            AnswerOptionVisualState.SELECTED
                        } else {
                            AnswerOptionVisualState.DEFAULT
                        }
                    )
                }
            )
        }
    }

    fun onActionButtonClick() {
        val state = _uiState.value
        when {
            state.isSessionCompleted && state.actionButtonText == TestAction.COMPLETE.label -> {
                _uiState.update { it.copy(shouldNavigateBack = true) }
            }

            state.answerPhase == AnswerPhase.SELECTING -> checkAnswer()
            state.answerPhase == AnswerPhase.REVEALED -> goToNextExercise()
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(shouldNavigateBack = false) }
    }

    fun onAudioClick() {
        val audioUrl = _uiState.value.audioUrl ?: return
        audioController.togglePlayback(audioUrl)
    }

    private fun checkAnswer() {
        val userId = currentUserId ?: return
        val exercise = currentExercise ?: return
        val selectedOptionId = _uiState.value.selectedOptionId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            submitExerciseAnswerUseCase(
                userId = userId,
                trainerId = trainerId,
                exerciseId = exercise.id,
                selectedOptionId = selectedOptionId
            ).onSuccess { result ->
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        answerPhase = AnswerPhase.REVEALED,
                        correctOptionId = result.correctOptionId,
                        isAnswerCorrect = result.isCorrect,
                        isSessionCompleted = result.isSessionCompleted,
                        completedExercises = result.completedExercises,
                        totalExercises = result.totalExercises,
                        actionButtonText = if (result.isSessionCompleted) {
                            TestAction.COMPLETE.label
                        } else {
                            TestAction.NEXT.label
                        },
                        options = state.options.map { option ->
                            option.copy(
                                visualState = resolveVisualState(
                                    optionId = option.id,
                                    selectedOptionId = selectedOptionId,
                                    correctOptionId = result.correctOptionId,
                                    isCorrect = result.isCorrect
                                )
                            )
                        }
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = error.message ?: "Failed to submit answer"
                    )
                }
            }
        }
    }

    private fun goToNextExercise() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            loadCurrentExercise(userId)
        }
    }

    private fun resolveVisualState(
        optionId: String,
        selectedOptionId: String,
        correctOptionId: String,
        isCorrect: Boolean
    ): AnswerOptionVisualState {
        return when {
            optionId == correctOptionId && optionId == selectedOptionId -> AnswerOptionVisualState.CORRECT
            optionId == selectedOptionId && !isCorrect -> AnswerOptionVisualState.INCORRECT
            optionId == correctOptionId && !isCorrect -> AnswerOptionVisualState.CORRECT_REVEALED
            optionId == correctOptionId -> AnswerOptionVisualState.CORRECT
            else -> AnswerOptionVisualState.DEFAULT
        }
    }

    private fun ExercisesAnswerOption.toUi() = ExerciseAnswerOptionUi(
        id = id,
        text = text,
        imageUrl = image,
        visualState = AnswerOptionVisualState.DEFAULT
    )

    override fun onCleared() {
        audioController.release()
        super.onCleared()
    }
}

data class ExerciseAnswerOptionUi(
    val id: String,
    val text: String?,
    val imageUrl: String?,
    val visualState: AnswerOptionVisualState
)

data class ExerciseUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val hasInitialized: Boolean = false,
    val error: String? = null,
    val trainerTitle: String = "",
    val exerciseText: String = "",
    val audioUrl: String? = null,
    val options: List<ExerciseAnswerOptionUi> = emptyList(),
    val selectedOptionId: String? = null,
    val answerPhase: AnswerPhase = AnswerPhase.SELECTING,
    val correctOptionId: String? = null,
    val isAnswerCorrect: Boolean? = null,
    val completedExercises: Int = 0,
    val totalExercises: Int = 0,
    val isSessionCompleted: Boolean = false,
    val actionButtonText: String = TestAction.CHECK.label,
    val shouldNavigateBack: Boolean = false
) {
    val progress: Float
        get() = if (totalExercises == 0) {
            0f
        } else {
            completedExercises.toFloat() / totalExercises.toFloat()
        }

    val isActionEnabled: Boolean
        get() = when {
            isSubmitting -> false
            isSessionCompleted && actionButtonText == TestAction.COMPLETE.label -> true
            answerPhase == AnswerPhase.REVEALED -> true
            answerPhase == AnswerPhase.SELECTING -> selectedOptionId != null
            else -> false
        }

    val actionButtonColorKey: ActionButtonColor
        get() = when {
            isSessionCompleted && actionButtonText == TestAction.COMPLETE.label -> ActionButtonColor.ACTIVE
            answerPhase == AnswerPhase.REVEALED -> ActionButtonColor.ACTIVE
            selectedOptionId != null -> ActionButtonColor.ACTIVE
            else -> ActionButtonColor.DISABLED
        }
}
