package com.example.solify.presentation.screens.trainings.components

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
import com.example.solify.presentation.screens.trainings.TrainingItemUi

@Composable
fun TrainingsSection(
    title: String,
    trainings: List<TrainingItemUi>,
    onTrainingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (trainings.isEmpty()) return

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(Modifier.height(20.dp))

        val lastIndex = trainings.lastIndex
        trainings.forEachIndexed { index, training ->
            TrainingCard(
                training = training,
                onPlayClick = { onTrainingClick(training.id) }
            )
            if (index != lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
