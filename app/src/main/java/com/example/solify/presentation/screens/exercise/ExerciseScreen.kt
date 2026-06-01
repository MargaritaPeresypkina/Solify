package com.example.solify.presentation.screens.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.solify.presentation.components.skeleton.QuizScreenSkeleton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.solify.presentation.screens.exercise.components.ExerciseAnswerOption
import com.example.solify.presentation.screens.exercise.components.ExerciseAudioButton
import com.example.solify.presentation.screens.exercise.components.ExerciseHeader
import com.example.solify.presentation.screens.test.ActionButtonColor
import com.example.solify.presentation.screens.test.AnswerPhase
import com.example.solify.presentation.screens.test.components.TestActionButton
import com.example.solify.presentation.ui.theme.LightYellow300

@Composable
fun ExerciseScreen(
    onNavigateBack: () -> Unit,
    onSessionCompleted: () -> Unit = onNavigateBack,
    modifier: Modifier = Modifier,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            viewModel.onNavigationHandled()
            onSessionCompleted()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier.then(Modifier.fillMaxSize())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = innerPadding.calculateTopPadding(), bottom = 22.dp)
                    .padding(top = 20.dp)
            ) {
                when {
                    uiState.isLoading -> {
                        QuizScreenSkeleton(
                            onCloseClick = onNavigateBack,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            ExerciseHeader(
                                title = uiState.trainerTitle,
                                progress = uiState.progress,
                                onCloseClick = onNavigateBack
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(top = 40.dp)
                                    .padding(horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = uiState.exerciseText,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(22.dp))

                                if (uiState.audioUrl != null) {
                                    ExerciseAudioButton(
                                        onClick = viewModel::onAudioClick
                                    )
                                }

                                Spacer(modifier = Modifier.height(40.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(9.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    uiState.options.forEach { option ->
                                        ExerciseAnswerOption(
                                            text = option.text,
                                            imageUrl = option.imageUrl,
                                            visualState = option.visualState,
                                            enabled = uiState.answerPhase == AnswerPhase.SELECTING,
                                            onClick = { viewModel.onOptionSelected(option.id) }
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                val containerColor = when (uiState.actionButtonColorKey) {
                                    ActionButtonColor.ACTIVE -> MaterialTheme.colorScheme.secondary
                                    ActionButtonColor.DISABLED -> MaterialTheme.colorScheme.secondaryContainer
                                }

                                TestActionButton(
                                    text = uiState.actionButtonText,
                                    enabled = uiState.isActionEnabled,
                                    containerColor = containerColor,
                                    onClick = viewModel::onActionButtonClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
