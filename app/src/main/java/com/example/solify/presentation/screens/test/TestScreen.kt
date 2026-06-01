package com.example.solify.presentation.screens.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.solify.presentation.screens.test.components.TestActionButton
import com.example.solify.presentation.screens.test.components.TestAnswerOption
import com.example.solify.presentation.screens.test.components.TestHeader
import com.example.solify.presentation.screens.test.components.TestHintOverlay
import com.example.solify.presentation.screens.test.components.TestShuffleButton
import androidx.compose.ui.res.stringResource
import com.example.solify.R

@Composable
fun TestScreen(
    onNavigateBack: () -> Unit,
    onTestCompleted: () -> Unit = onNavigateBack,
    modifier: Modifier = Modifier,
    viewModel: TestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(uiState.shouldNavigateToLesson) {
        if (uiState.shouldNavigateToLesson) {
            viewModel.onNavigationHandled()
            onTestCompleted()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier.then(Modifier.fillMaxSize())) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = innerPadding.calculateTopPadding(), bottom = 22.dp)
                    .padding(top = 20.dp)
            ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                }

                uiState.isStartScreenVisible -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TestHeader(
                            title = uiState.testTitle,
                            progress = 0f,
                            onCloseClick = onNavigateBack,
                            onHintClick = {}
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.testTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            TestActionButton(
                                text = stringResource(R.string.test_start),
                                enabled = true,
                                containerColor = MaterialTheme.colorScheme.secondary,
                                onClick = viewModel::onStartTestClick
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TestHeader(
                            title = uiState.testTitle,
                            progress = uiState.progress,
                            onCloseClick = onNavigateBack,
                            onHintClick = viewModel::onHintClick
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TestShuffleButton(
                            isActive = uiState.randomOrderEnabled,
                            enabled = !uiState.isSubmitting && !uiState.isTestCompleted,
                            onClick = viewModel::onShuffleClick
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(top = 24.dp)
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.questionText,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            uiState.questionImageUrl?.let { imageUrl ->
                                Spacer(modifier = Modifier.height(22.dp))
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(280.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.FillWidth
                                )
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(9.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                uiState.options.forEach { option ->
                                    TestAnswerOption(
                                        text = option.text,
                                        visualState = option.visualState,
                                        enabled = uiState.answerPhase == AnswerPhase.SELECTING,
                                        onClick = { viewModel.onOptionSelected(option.id) }
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
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

            if (uiState.isHintVisible) {
                TestHintOverlay(
                    hintText = uiState.hintText,
                    onCloseClick = viewModel::onHintDismiss,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
