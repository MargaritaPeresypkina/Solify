package com.example.solify.presentation.screens.lessons

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.solify.presentation.navigation.BottomNavigationBar
import com.example.solify.presentation.screens.lessons.components.LessonsHeader
import com.example.solify.presentation.screens.lessons.components.LessonsSection

@Composable
fun LessonsScreen(
    modifier: Modifier = Modifier,
    onLessonClick: (String) -> Unit,
    navController: NavController,
    viewModel: LessonsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshProgress()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            Column(
                modifier = modifier.fillMaxSize()
            ) {
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
                    uiState.error != null && !hasAnyLessons(uiState) -> {
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
                                Spacer(Modifier.height(33.dp))
                            }

                            if (uiState.beginnerLessons.isNotEmpty()) {
                                item(key = "section_beginner") {
                                    LessonsSection(
                                        title = "BEGINNER",
                                        lessons = uiState.beginnerLessons,
                                        onLessonClick = onLessonClick
                                    )
                                }
                            }

                            if (uiState.intermediateLessons.isNotEmpty()) {
                                item(key = "section_intermediate") {
                                    LessonsSection(
                                        title = "INTERMEDIATE",
                                        lessons = uiState.intermediateLessons,
                                        onLessonClick = onLessonClick
                                    )
                                }
                            }

                            if (uiState.advancedLessons.isNotEmpty()) {
                                item(key = "section_advanced") {
                                    LessonsSection(
                                        title = "ADVANCED",
                                        lessons = uiState.advancedLessons,
                                        onLessonClick = onLessonClick
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

private fun hasAnyLessons(uiState: LessonsUiState): Boolean {
    return uiState.beginnerLessons.isNotEmpty() ||
            uiState.intermediateLessons.isNotEmpty() ||
            uiState.advancedLessons.isNotEmpty()
}