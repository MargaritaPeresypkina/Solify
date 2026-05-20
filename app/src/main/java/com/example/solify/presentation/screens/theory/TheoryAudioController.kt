package com.example.solify.presentation.screens.theory

import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TheoryAudioUiState(
    val activeUrl: String? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isMuted: Boolean = false,
    val progress: Float = 0f
)

class TheoryAudioController {

    private val _state = MutableStateFlow(TheoryAudioUiState())
    val state: StateFlow<TheoryAudioUiState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var isPreparing = false

    fun togglePlayback(url: String) {
        if (isPreparing) return

        val current = _state.value
        when {
            current.activeUrl == url && current.isPlaying -> pause()
            current.activeUrl == url && mediaPlayer != null -> resume()
            else -> startPlayback(url)
        }
    }

    fun toggleMute() {
        val muted = !_state.value.isMuted
        val volume = if (muted) 0f else 1f
        mediaPlayer?.setVolume(volume, volume)
        _state.update { it.copy(isMuted = muted) }
    }

    fun updateProgress() {
        val player = mediaPlayer ?: return
        if (!player.isPlaying) return
        val duration = player.duration
        if (duration <= 0) return
        val progress = player.currentPosition.toFloat() / duration.toFloat()
        _state.update { it.copy(progress = progress.coerceIn(0f, 1f)) }
    }

    fun release() {
        isPreparing = false
        mediaPlayer?.runCatching {
            stop()
            release()
        }
        mediaPlayer = null
        _state.value = TheoryAudioUiState()
    }

    private fun pause() {
        mediaPlayer?.pause()
        _state.update { it.copy(isPlaying = false) }
    }

    private fun resume() {
        mediaPlayer?.start()
        _state.update { it.copy(isPlaying = true) }
    }

    private fun startPlayback(url: String) {
        releasePlayerOnly()
        isPreparing = true
        _state.update {
            TheoryAudioUiState(
                activeUrl = url,
                isPlaying = false,
                isLoading = true,
                isMuted = it.isMuted,
                progress = 0f
            )
        }

        val player = MediaPlayer()
        mediaPlayer = player

        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )

        player.setOnPreparedListener { preparedPlayer ->
            isPreparing = false
            val volume = if (_state.value.isMuted) 0f else 1f
            preparedPlayer.setVolume(volume, volume)
            preparedPlayer.start()
            _state.update {
                it.copy(
                    activeUrl = url,
                    isPlaying = true,
                    isLoading = false
                )
            }
        }

        player.setOnCompletionListener {
            _state.update {
                it.copy(isPlaying = false, progress = 1f)
            }
        }

        player.setOnErrorListener { _, _, _ ->
            isPreparing = false
            releasePlayerOnly()
            _state.value = TheoryAudioUiState(isMuted = _state.value.isMuted)
            true
        }

        runCatching {
            player.setDataSource(url)
            player.prepareAsync()
        }.onFailure {
            isPreparing = false
            releasePlayerOnly()
            _state.value = TheoryAudioUiState(isMuted = _state.value.isMuted)
        }
    }

    private fun releasePlayerOnly() {
        isPreparing = false
        mediaPlayer?.runCatching {
            stop()
            release()
        }
        mediaPlayer = null
    }
}
