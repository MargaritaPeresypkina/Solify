package com.example.solify.presentation.screens.lesson.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.solify.presentation.screens.lesson.TestItemUi
import com.example.solify.presentation.screens.lessons.LessonItemUi
import com.example.solify.presentation.screens.lessons.components.LessonCard

@Composable
fun LessonTestCard(
    test: TestItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LessonCard(
        lesson = LessonItemUi(
            id = test.id,
            title = test.title,
            description = test.description,
            level = com.example.solify.domain.entities.lesson.Level.BEGINNER,
            status = test.status,
            iconRes = test.iconRes
        ),
        onClick = onClick,
        modifier = modifier
    )
}
