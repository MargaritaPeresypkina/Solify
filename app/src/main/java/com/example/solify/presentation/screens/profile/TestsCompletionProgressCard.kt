package com.example.solify.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.solify.presentation.ui.theme.Grey100

private val CardShape = RoundedCornerShape(16.dp)
private val ProgressCircleSize = 50.dp
private val ProgressStrokeWidth = 2.dp

@Composable
fun TestsCompletionProgressCard(
    completionPercent: Int,
    completedTestsCount: Int,
    userLevel: String,
    onSeeProgressClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CardShape)
            .background(Grey100)
            .padding(start = 15.dp, end = 15.dp, top = 25.dp, bottom = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularTestsProgressIndicator(
            percent = completionPercent,
            modifier = Modifier.size(ProgressCircleSize)
        )
        Column(
            modifier = Modifier
                .padding(start = 20.dp)
                .weight(1f)
        ) {
            Text(
                text = "Studied at level - ${userLevel.lowercase()}",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "You have passed $completedTestsCount tests",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "See your progress",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .offset(x = (-10).dp)
                    .clickable(onClick = onSeeProgressClick)
            )
        }
    }
}

@Composable
private fun CircularTestsProgressIndicator(
    percent: Int,
    modifier: Modifier = Modifier
) {
    val percentText = "${percent.coerceIn(0, 100)}%"

    CircularProgressRing(
        percent = percent,
        modifier = modifier,
        strokeWidth = ProgressStrokeWidth
    ) {
        Text(
            text = percentText,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
