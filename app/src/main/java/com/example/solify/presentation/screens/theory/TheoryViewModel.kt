package com.example.solify.presentation.screens.theory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.domain.entities.lesson.TheoryContent
import com.example.solify.domain.usecases.lessons.GetTheoryItemByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TheoryContentUi(
    val type: TheoryContentType,
    val payload: String
)

enum class TheoryContentType {
    TEXT, IMAGE, AUDIO
}

data class TheoryUiState(
    val title: String = "",
    val contentItems: List<TheoryContentUi> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val audioState: TheoryAudioUiState = TheoryAudioUiState()
)

@HiltViewModel
class TheoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTheoryItemByIdUseCase: GetTheoryItemByIdUseCase
) : ViewModel() {

    private val theoryItemId: String = savedStateHandle.get<String>("theory_item_id").orEmpty()
    private val audioController = TheoryAudioController()
    private var progressJob: Job? = null

    private val _uiState = MutableStateFlow(TheoryUiState())
    val uiState: StateFlow<TheoryUiState> = _uiState.asStateFlow()

    init {
        loadTheory()
        viewModelScope.launch {
            audioController.state.collect { audioState ->
                _uiState.update { it.copy(audioState = audioState) }
            }
        }
    }

    fun onAudioToggle(url: String) {
        audioController.togglePlayback(url)
        startProgressUpdates()
    }

    fun onAudioMuteToggle() {
        audioController.toggleMute()
    }

    fun onNavigateAway() {
        progressJob?.cancel()
        audioController.release()
    }

    private fun loadTheory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getTheoryItemByIdUseCase(theoryItemId)
                .onSuccess { theoryItem ->
                    _uiState.update {
                        it.copy(
                            title = theoryItem.title,
                            contentItems = theoryItem.content.map { content -> content.toUi() },
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load theory"
                        )
                    }
                }
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                audioController.updateProgress()
                delay(200)
            }
        }
    }

    private fun TheoryContent.toUi(): TheoryContentUi = when (this) {
        is TheoryContent.Text -> TheoryContentUi(TheoryContentType.TEXT, text)
        is TheoryContent.Image -> TheoryContentUi(TheoryContentType.IMAGE, imageUrl)
        is TheoryContent.Audio -> TheoryContentUi(TheoryContentType.AUDIO, audioUrl)
    }

    override fun onCleared() {
        progressJob?.cancel()
        audioController.release()
        super.onCleared()
    }
}
