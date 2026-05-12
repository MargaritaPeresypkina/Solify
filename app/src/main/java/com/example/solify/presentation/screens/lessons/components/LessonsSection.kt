package com.example.solify.presentation.screens.lessons.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solify.presentation.screens.lessons.LessonItemUi

@Composable
fun LessonsSection(
    title: String,
    lessons: List<LessonItemUi>,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (lessons.isEmpty()) return

    Column(modifier = modifier) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(Modifier.height(8.dp))
        val listSize = lessons.size - 1
        lessons.forEachIndexed { index, lesson ->
            LessonCard(
                lesson = lesson,
                onClick = { onLessonClick(lesson.id) }
            )
            if(listSize != index) {
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}