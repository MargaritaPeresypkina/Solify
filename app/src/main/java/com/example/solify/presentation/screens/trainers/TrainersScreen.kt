package com.example.solify.presentation.screens.trainers

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
import com.example.solify.presentation.components.skeleton.TrainersListSkeleton
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
import com.example.solify.presentation.screens.lesson.components.LessonHeader
import com.example.solify.presentation.screens.trainers.components.TrainersListSection

@Composable
fun TrainersScreen(
    trainingId: String,
    onNavigateBack: () -> Unit,
    onTrainerClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TrainersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = innerPadding.calculateTopPadding())
                .padding(top = 10.dp)
        ) {
            when {
                    shouldShowTrainersSkeleton(uiState) -> {
                        TrainersListSkeleton(
                            onBackClick = onNavigateBack,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    uiState.error != null && !uiState.hasLoadedOnce -> {
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
                        Column(modifier = Modifier.fillMaxSize()) {
                        LessonHeader(
                            title = uiState.trainingTitle.uppercase(),
                            onBackClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(24.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            item(key = "top_spacer") {
                                Spacer(Modifier.height(34.dp))
                            }

                            if (uiState.trainers.isNotEmpty()) {
                                item(key = "trainers_list") {
                                    TrainersListSection(
                                        trainers = uiState.trainers,
                                        onTrainerClick = onTrainerClick
                                    )
                                }
                            }

                            item(key = "bottom_spacer") {
                                Spacer(modifier = Modifier.padding(bottom = 24.dp))
                            }
                        }
                        }
                    }
                }
        }
    }
}

private fun shouldShowTrainersSkeleton(uiState: com.example.solify.presentation.screens.trainers.TrainersUiState): Boolean {
    if (uiState.error != null && !uiState.hasLoadedOnce) return false
    return uiState.isLoading && !uiState.hasLoadedOnce
}
