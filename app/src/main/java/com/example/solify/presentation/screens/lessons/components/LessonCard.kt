package com.example.solify.presentation.screens.lessons.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.solify.R
import com.example.solify.domain.entities.progress.Status
import com.example.solify.presentation.screens.lessons.LessonItemUi
import com.example.solify.presentation.ui.theme.Brown300
import com.example.solify.presentation.ui.theme.Grey100
import com.example.solify.presentation.ui.theme.Grey300
import com.example.solify.presentation.ui.theme.White300

@Composable
fun LessonCard(
    lesson: LessonItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Grey100
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(start = 12.dp, end = 25.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Иконка - начало
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(getStatusColor(lesson.status).second),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(getStatusColor(lesson.status).first),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = lesson.iconRes),
                        contentDescription = lesson.status.name,
                        modifier = Modifier.size(if(lesson.status.name == "COMPLETED") 20.dp else 14.dp),
                        tint = if(lesson.status.name == "NOT_STARTED") Brown300 else White300
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(15.dp))
            
            // информация
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.padding(4.dp))
                
                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))

            // кнопка
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onClick() }
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    contentDescription = stringResource(R.string.go_to_lesson),
                    modifier = Modifier.size(10.5.dp),
                    tint = White300
                )
            }
        }
    }
}

@Composable
private fun getStatusColor(status: Status): Pair<Color, Color> {
    return when (status) {
        Status.COMPLETED -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.tertiary
        Status.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onTertiary
        Status.NOT_STARTED -> Grey300 to Grey100
    }
}