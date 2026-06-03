package com.example.solify.presentation.screens.your_progress

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.solify.R
import com.example.solify.presentation.components.skeleton.ProfileScreenSkeleton
import com.example.solify.presentation.screens.profile.TestsCompletionProgressCard
import com.example.solify.presentation.ui.theme.Brown300
import com.example.solify.presentation.ui.theme.White300

private val HeaderIconSize = 94
private val StatCardShape = RoundedCornerShape(20.dp)

@Composable
fun YourProgressScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: YourProgressViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = innerPadding.calculateTopPadding())
                .padding(top = 10.dp),
        ) {
            if (uiState.isLoading) {
                ProfileScreenSkeleton(modifier = Modifier.fillMaxSize())
                return@Box
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
            ) {
                Box(modifier = Modifier.zIndex(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cross),
                            contentDescription = stringResource(R.string.close),
                            modifier = Modifier
                                .size(12.dp)
                                .clickable(onClick = onNavigateBack),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.music_player_dvd_svgrepo_com),
                            contentDescription = null,
                            modifier = Modifier
                                .size(HeaderIconSize.dp)
                                .zIndex(1f)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (HeaderIconSize / 2).dp)
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height((HeaderIconSize / 2 + 18).dp))
                    Text(
                        text = stringResource(R.string.your_progress),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(16.dp))
                    TestsCompletionProgressCard(
                        completionPercent = uiState.testsCompletionPercent,
                        userLevel = uiState.userLevel,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    ProgressCountCard(
                        count = uiState.completedTestsCount,
                        label = stringResource(R.string.tests_label),
                        description = stringResource(R.string.progress_tests_description),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    ProgressCountCard(
                        count = uiState.completedExercisesCount,
                        label = stringResource(R.string.exercises_label),
                        description = stringResource(R.string.progress_exercises_description),
                        backgroundColor = Brown300,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ProgressCountCard(
    count: Int,
    label: String,
    description: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .dropShadow(
                shape = StatCardShape,
                shadow = Shadow(
                    radius = 20.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    offset = DpOffset(0.dp, 0.dp)
                )
            )
            .clip(StatCardShape)
            .background(backgroundColor)
            .padding(horizontal = 38.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = count.toString(),
            color = White300,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp,
            ),
        )
        Text(
            text = label,
            color = White300,
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.height(19.dp))
        Text(
            text = description,
            color = White300,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
        )
    }
}
