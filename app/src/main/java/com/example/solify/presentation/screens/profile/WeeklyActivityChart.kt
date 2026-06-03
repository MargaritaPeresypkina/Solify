package com.example.solify.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.solify.domain.entities.progress.WeeklyActivityDay
import com.example.solify.presentation.ui.theme.Black100
import com.example.solify.presentation.ui.theme.Brown300
import com.example.solify.presentation.ui.theme.Burgundy200
import com.example.solify.presentation.ui.theme.Grey300
import com.example.solify.presentation.ui.theme.LightYellow300
import com.example.solify.presentation.ui.theme.White300
import com.example.solify.presentation.ui.theme.Yellow200

private val ChartCardHeight = 209.dp
private val ChartBarWidth = 24.dp
private val ChartLabelAreaHeight = 20.dp
private val ChartDayLabelAreaHeight = 18.dp
private val ChartCardShape = RoundedCornerShape(24.dp)

@Composable
fun WeeklyActivityChart(
    days: List<WeeklyActivityDay>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ChartCardHeight)
            .dropShadow(
                shape = ChartCardShape,
                shadow = Shadow(
                    radius = 30.dp,
                    color = Black100,
                    offset = DpOffset(0.dp, 0.dp)
                )
            )
            .clip(ChartCardShape)
            .background(White300)
            .padding(top = 11.dp, bottom = 14.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEach { day ->
                WeeklyActivityBar(day = day)
            }
        }
    }
}

@Composable
private fun WeeklyActivityBar(day: WeeklyActivityDay) {
    val style = activityStyle(day.completedTestsCount)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = style.rangeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = Grey300,
            modifier = Modifier.height(ChartLabelAreaHeight)
        )
        Box(
            modifier = Modifier
                .width(ChartBarWidth)
                .height(style.barHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(style.barColor)
        )
        Text(
            text = day.dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = Grey300,
            modifier = Modifier
                .padding(top = 6.dp)
                .height(ChartDayLabelAreaHeight)
        )
    }
}

private data class ActivityBarStyle(
    val barHeight: Dp,
    val barColor: Color,
    val rangeLabel: String
)

private fun activityStyle(completedTests: Int): ActivityBarStyle = when {
    completedTests > 7 -> ActivityBarStyle(140.dp, Brown300, "> 7")
    completedTests >= 5 -> ActivityBarStyle(112.dp, Burgundy200, "5-6")
    completedTests >= 3 -> ActivityBarStyle(84.dp, Yellow200, "3-4")
    completedTests >= 1 -> ActivityBarStyle(56.dp, LightYellow300, "1-2")
    else -> ActivityBarStyle(28.dp, Grey300, "0")
}
