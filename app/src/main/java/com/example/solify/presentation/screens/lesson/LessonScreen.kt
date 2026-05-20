package com.example.solify.presentation.screens.lesson

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
import com.example.solify.presentation.screens.lesson.components.LessonExercisesSection
import com.example.solify.presentation.screens.lesson.components.LessonHeader
import com.example.solify.presentation.screens.lesson.components.LessonTheorySection

@Composable
fun LessonScreen(
    lessonId: String,
    onNavigateBack: () -> Unit,
    onTheoryClick: (String) -> Unit = {},
    onTestClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LessonViewModel = hiltViewModel()
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
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LessonHeader(
                    title = uiState.lessonTitle.ifEmpty { lessonId },
                    onBackClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                when {
                    uiState.isLoading && !uiState.hasLoadedOnce -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
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
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            item(key = "top_spacer") {
                                Spacer(Modifier.height(34.dp))
                            }

                            if (uiState.theoryItems.isNotEmpty()) {
                                item(key = "theory_section") {
                                    LessonTheorySection(
                                        items = uiState.theoryItems,
                                        onTheoryClick = { theoryItemId ->
                                            onTheoryClick(theoryItemId)
                                        }
                                    )
                                }
                            }

                            if (uiState.tests.isNotEmpty()) {
                                item(key = "exercises_section") {
                                    LessonExercisesSection(
                                        tests = uiState.tests,
                                        onTestClick = onTestClick
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
