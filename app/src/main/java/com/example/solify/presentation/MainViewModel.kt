package com.example.solify.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.domain.usecases.auth.AuthState
import com.example.solify.domain.usecases.auth.GetSessionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getSessionStatusUseCase: GetSessionStatusUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch(Dispatchers.IO) {
            getSessionStatusUseCase()
                .catch { error ->
                    Log.e("MainViewModel", "Error checking session", error)
                    _authState.update { AuthState.Unauthorized }
                }
                .collect { state ->
                    Log.d("MainViewModel", "AuthState: $state")
                    _authState.update { state }
                }
        }
    }
}