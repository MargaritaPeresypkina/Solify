package com.example.solify.presentation.screens.trainings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.solify.presentation.navigation.BottomNavigationBar
import com.example.solify.presentation.screens.lessons.components.LessonsHeader
import com.example.solify.presentation.screens.trainings.components.TrainingsSection

@Composable
fun TrainingScreen(
    navController: NavController,
    onTrainingClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TrainingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = innerPadding.calculateTopPadding())
                .padding(top = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LessonsHeader(
                    avatarUrl = uiState.userAvatarUrl,
                    badgeRes = uiState.userBadgeRes,
                    level = uiState.userLevel,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))

                when {
                    uiState.isLoading && !uiState.hasLoadedOnce -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.error != null && !hasAnyTrainings(uiState) -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            item(key = "top_spacer") {
                                Spacer(Modifier.height(28.dp))
                            }

                            if (uiState.earTrainings.isNotEmpty()) {
                                item(key = "section_ear") {
                                    TrainingsSection(
                                        title = "Hearing trainings",
                                        trainings = uiState.earTrainings,
                                        onTrainingClick = onTrainingClick
                                    )
                                }
                            }

                            if (uiState.rhythmTrainings.isNotEmpty()) {
                                item(key = "section_rhythm") {
                                    TrainingsSection(
                                        title = "RHYTHM TRAININGS",
                                        trainings = uiState.rhythmTrainings,
                                        onTrainingClick = onTrainingClick
                                    )
                                }
                            }

                            item(key = "bottom_spacer") {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun hasAnyTrainings(uiState: TrainingsUiState): Boolean {
    return uiState.earTrainings.isNotEmpty() || uiState.rhythmTrainings.isNotEmpty()
}
