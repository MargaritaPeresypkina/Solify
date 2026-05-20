package com.example.solify.presentation.screens.lesson.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.solify.R
import com.example.solify.presentation.screens.lesson.TestItemUi
import com.example.solify.presentation.screens.lesson.TheoryItemUi

@Composable
fun LessonTheorySection(
    items: List<TheoryItemUi>,
    onTheoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(modifier = modifier) {
        val lastIndex = items.lastIndex
        items.forEachIndexed { index, item ->
            TheoryCard(
                item = item,
                onClick = { onTheoryClick(item.id) }
            )
            if (index != lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(29.dp))
    }
}

@Composable
fun LessonExercisesSection(
    tests: List<TestItemUi>,
    onTestClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tests.isEmpty()) return

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.exercises),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(Modifier.height(16.dp))

        val lastIndex = tests.lastIndex
        tests.forEachIndexed { index, test ->
            LessonTestCard(
                test = test,
                onClick = { onTestClick(test.id) }
            )
            if (index != lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(29.dp))
    }
}
