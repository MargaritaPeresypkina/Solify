package com.example.solify.presentation.screens.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.domain.entities.lesson.AnswerOption
import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.usecases.tests.GetNextQuestionUseCase
import com.example.solify.domain.usecases.tests.StartTestUseCase
import com.example.solify.domain.usecases.tests.SubmitAnswerUseCase
import com.example.solify.domain.usecases.user.ObserveCurrentUserUseCase
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
class TestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
    private val startTestUseCase: StartTestUseCase,
    private val getNextQuestionUseCase: GetNextQuestionUseCase,
    private val submitAnswerUseCase: SubmitAnswerUseCase
) : ViewModel() {

    private val lessonId: String = savedStateHandle.get<String>("lesson_id").orEmpty()
    private val testId: String = savedStateHandle.get<String>("test_id").orEmpty()

    private val _uiState = MutableStateFlow(TestUiState())
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null
    private var currentQuestion: Question? = null
    private var totalQuestions: Int = 0

    init {
        observeCurrentUserUseCase()
            .filterNotNull()
            .distinctUntilChanged { old, new -> old.id == new.id }
            .onEach { user ->
                currentUserId = user.id
                if (!_uiState.value.hasInitialized) {
                    initializeTest(user.id)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun initializeTest(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val test = lessonRepository.getTestById(testId, lessonId).getOrElse { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message ?: "Test not found")
                }
                return@launch
            }

            totalQuestions = test.questionsIds.size
            _uiState.update { it.copy(testTitle = test.title, totalQuestions = totalQuestions) }

            startTestUseCase(userId, lessonId, testId).onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
                return@launch
            }

            observeTestProgress(userId)
            loadCurrentQuestion(userId)
        }
    }

    private fun observeTestProgress(userId: String) {
        progressRepository.observeTestProgress(userId, testId)
            .onEach { progress ->
                val completed = progress?.completedQuestions?.size ?: 0
                val total = totalQuestions.takeIf { it > 0 }
                    ?: ((progress?.completedQuestions?.size ?: 0) + (progress?.pendingQuestions?.size ?: 0))
                _uiState.update {
                    it.copy(
                        completedQuestions = completed,
                        totalQuestions = total.coerceAtLeast(1),
                        hasInitialized = true
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun loadCurrentQuestion(userId: String) {
        getNextQuestionUseCase(userId, lessonId, testId)
            .onSuccess { question ->
                if (question == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No questions available",
                            isTestCompleted = true
                        )
                    }
                    return
                }

                currentQuestion = question
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        questionText = question.text,
                        hintText = question.hint,
                        questionImageUrl = question.imageUrl?.takeIf { url -> url.isNotBlank() },
                        isHintVisible = false,
                        options = question.options.shuffled().map { option -> option.toUi() },
                        selectedOptionId = null,
                        answerPhase = AnswerPhase.SELECTING,
                        correctOptionId = null,
                        isAnswerCorrect = null,
                        isTestCompleted = false,
                        actionButtonText = TestAction.CHECK.label
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message ?: "Failed to load question")
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
            state.isTestCompleted && state.actionButtonText == TestAction.COMPLETE.label -> {
                // Navigation handled in screen via callback when state signals completion
                _uiState.update { it.copy(shouldNavigateToLesson = true) }
            }

            state.answerPhase == AnswerPhase.SELECTING -> checkAnswer()
            state.answerPhase == AnswerPhase.REVEALED -> goToNextQuestion()
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(shouldNavigateToLesson = false) }
    }

    fun onHintClick() {
        if (_uiState.value.hintText.isNotBlank()) {
            _uiState.update { it.copy(isHintVisible = true) }
        }
    }

    fun onHintDismiss() {
        _uiState.update { it.copy(isHintVisible = false) }
    }

    private fun checkAnswer() {
        val userId = currentUserId ?: return
        val question = currentQuestion ?: return
        val selectedOptionId = _uiState.value.selectedOptionId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            submitAnswerUseCase(
                userId = userId,
                lessonId = lessonId,
                testId = testId,
                questionId = question.id,
                selectedOptionId = selectedOptionId
            ).onSuccess { result ->
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        answerPhase = AnswerPhase.REVEALED,
                        correctOptionId = result.correctOptionId,
                        isAnswerCorrect = result.isCorrect,
                        isTestCompleted = result.isTestCompleted,
                        completedQuestions = result.completedQuestions,
                        totalQuestions = result.totalQuestions,
                        actionButtonText = if (result.isTestCompleted) {
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
                    it.copy(isSubmitting = false, error = error.message ?: "Failed to submit answer")
                }
            }
        }
    }

    private fun goToNextQuestion() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            loadCurrentQuestion(userId)
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

    private fun AnswerOption.toUi() = AnswerOptionUi(
        id = id,
        text = text,
        visualState = AnswerOptionVisualState.DEFAULT
    )
}

enum class AnswerPhase {
    SELECTING,
    REVEALED
}

enum class TestAction(val label: String) {
    CHECK("Check"),
    NEXT("Next"),
    COMPLETE("Complete")
}

enum class AnswerOptionVisualState {
    DEFAULT,
    SELECTED,
    CORRECT,
    INCORRECT,
    CORRECT_REVEALED
}

data class AnswerOptionUi(
    val id: String,
    val text: String,
    val visualState: AnswerOptionVisualState
)

data class TestUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val hasInitialized: Boolean = false,
    val error: String? = null,
    val testTitle: String = "",
    val questionText: String = "",
    val hintText: String = "",
    val isHintVisible: Boolean = false,
    val questionImageUrl: String? = null,
    val options: List<AnswerOptionUi> = emptyList(),
    val selectedOptionId: String? = null,
    val answerPhase: AnswerPhase = AnswerPhase.SELECTING,
    val correctOptionId: String? = null,
    val isAnswerCorrect: Boolean? = null,
    val completedQuestions: Int = 0,
    val totalQuestions: Int = 1,
    val isTestCompleted: Boolean = false,
    val actionButtonText: String = TestAction.CHECK.label,
    val shouldNavigateToLesson: Boolean = false
) {
    val progress: Float
        get() = if (totalQuestions == 0) 0f else completedQuestions.toFloat() / totalQuestions.toFloat()

    val isActionEnabled: Boolean
        get() = when {
            isSubmitting -> false
            isTestCompleted && actionButtonText == TestAction.COMPLETE.label -> true
            answerPhase == AnswerPhase.REVEALED -> true
            answerPhase == AnswerPhase.SELECTING -> selectedOptionId != null
            else -> false
        }

    val actionButtonColorKey: ActionButtonColor
        get() = when {
            isTestCompleted && actionButtonText == TestAction.COMPLETE.label -> ActionButtonColor.ACTIVE
            answerPhase == AnswerPhase.REVEALED -> ActionButtonColor.ACTIVE
            selectedOptionId != null -> ActionButtonColor.ACTIVE
            else -> ActionButtonColor.DISABLED
        }
}

enum class ActionButtonColor {
    ACTIVE,
    DISABLED
}
