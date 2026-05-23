package com.example.solify.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.domain.session.SessionManager
import com.example.solify.domain.sync.UserDataSyncCoordinator
import com.example.solify.domain.usecases.auth.AuthState
import com.example.solify.domain.usecases.auth.GetSessionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getSessionStatusUseCase: GetSessionStatusUseCase,
    private val sessionManager: SessionManager,
    private val userDataSyncCoordinator: UserDataSyncCoordinator
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            getSessionStatusUseCase()
                .distinctUntilChanged()
                .catch { error ->
                    Log.e("MainViewModel", "Error checking session", error)
                    _authState.update { AuthState.Unauthorized }
                }
                .collect { state ->
                    _authState.update { state }
                    if (state == AuthState.Authorized) {
                        prefetchUserData()
                    }
                }
        }
    }

    private fun prefetchUserData() {
        viewModelScope.launch {
            val userId = sessionManager.getCurrentUserId() ?: return@launch
            userDataSyncCoordinator.ensureSynced(userId)
        }
    }
}