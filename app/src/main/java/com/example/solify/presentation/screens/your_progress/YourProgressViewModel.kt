package com.example.solify.presentation.screens.your_progress

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.domain.entities.progress.TestsCompletionOverview
import com.example.solify.domain.usecases.progress.ObserveCompletedExercisesCountUseCase
import com.example.solify.domain.usecases.progress.ObserveTestsCompletionPercentUseCase
import com.example.solify.domain.usecases.user.GetUserBadgeUseCase
import com.example.solify.domain.usecases.user.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class YourProgressViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getUserBadgeUseCase: GetUserBadgeUseCase,
    private val observeTestsCompletionPercentUseCase: ObserveTestsCompletionPercentUseCase,
    private val observeCompletedExercisesCountUseCase: ObserveCompletedExercisesCountUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(YourProgressUiState(isLoading = true))
    val uiState: StateFlow<YourProgressUiState> = _uiState.asStateFlow()

    private val userFlow = observeCurrentUserUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val testsCompletionFlow = userFlow
        .flatMapLatest { user ->
            if (user == null) emptyFlow() else observeTestsCompletionPercentUseCase(user.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TestsCompletionOverview()
        )

    private val completedExercisesFlow = userFlow
        .flatMapLatest { user ->
            if (user == null) emptyFlow() else observeCompletedExercisesCountUseCase(user.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        viewModelScope.launch {
            testsCompletionFlow.collect { overview ->
                _uiState.update {
                    it.copy(
                        testsCompletionPercent = overview.percent,
                        completedTestsCount = overview.completedTests,
                    )
                }
            }
        }

        viewModelScope.launch {
            completedExercisesFlow.collect { count ->
                _uiState.update { it.copy(completedExercisesCount = count) }

            }
        }

        viewModelScope.launch {
            userFlow.collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }
                val badge = getUserBadgeUseCase(user.id)
                _uiState.update {
                    it.copy(
                        userLevel = badge.levelName,
                        isLoading = false,
                    )
                }
            }
        }
    }
}

@Stable
data class YourProgressUiState(
    val isLoading: Boolean = true,
    val userLevel: String = "Let's try",
    val testsCompletionPercent: Int = 0,
    val completedTestsCount: Int = 0,
    val completedExercisesCount: Int = 0,
)
