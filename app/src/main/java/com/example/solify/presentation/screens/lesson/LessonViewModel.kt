package com.example.solify.presentation.screens.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.R
import com.example.solify.domain.entities.progress.Status
import com.example.solify.domain.usecases.lessons.ObserveLessonDetailsUseCase
import com.example.solify.domain.usecases.lessons.TestWithStatus
import com.example.solify.domain.usecases.lessons.TheoryItemSummary
import com.example.solify.domain.usecases.progress.SyncTestsProgressUseCase
import com.example.solify.domain.usecases.user.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LessonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observeLessonDetailsUseCase: ObserveLessonDetailsUseCase,
    private val syncTestsProgressUseCase: SyncTestsProgressUseCase
) : ViewModel() {

    private val lessonId: String = savedStateHandle.get<String>("lesson_id").orEmpty()

    private val _uiState = MutableStateFlow(LessonUiState(lessonId = lessonId))
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    private val currentUserId = MutableStateFlow<String?>(null)
    private var hasSyncedTestsOnce = false

    init {
        observeCurrentUserUseCase()
            .filterNotNull()
            .distinctUntilChanged { old, new -> old.id == new.id }
            .onEach { user ->
                currentUserId.value = user.id
                if (!hasSyncedTestsOnce) {
                    hasSyncedTestsOnce = true
                    viewModelScope.launch {
                        runCatching { syncTestsProgressUseCase(user.id) }
                    }
                }
            }
            .launchIn(viewModelScope)

        currentUserId
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                observeLessonDetailsUseCase(userId, lessonId)
            }
            .onEach { result ->
                result.fold(
                    onSuccess = { details ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                hasLoadedOnce = true,
                                lessonTitle = details.title,
                                theoryItems = details.theoryItems.map { item -> item.toUi() },
                                tests = details.tests.map { test -> test.toUi() }
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load lesson"
                            )
                        }
                    }
                )
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load lesson"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun TheoryItemSummary.toUi() = TheoryItemUi(
        id = id,
        title = title,
        description = description
    )

    private fun TestWithStatus.toUi() = TestItemUi(
        id = id,
        title = title,
        description = description,
        status = status,
        iconRes = status.toIconRes()
    )

    private fun Status.toIconRes(): Int = when (this) {
        Status.COMPLETED -> R.drawable.tick_done
        Status.IN_PROGRESS -> R.drawable.lightning_in_progress
        Status.NOT_STARTED -> R.drawable.lock_uncomplete
    }
}

data class LessonUiState(
    val lessonId: String = "",
    val lessonTitle: String = "",
    val isLoading: Boolean = true,
    val hasLoadedOnce: Boolean = false,
    val error: String? = null,
    val theoryItems: List<TheoryItemUi> = emptyList(),
    val tests: List<TestItemUi> = emptyList()
)

data class TheoryItemUi(
    val id: String,
    val title: String,
    val description: String
)

data class TestItemUi(
    val id: String,
    val title: String,
    val description: String,
    val status: Status,
    val iconRes: Int
)
