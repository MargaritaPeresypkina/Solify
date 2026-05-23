package com.example.solify.presentation.screens.trainers.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solify.presentation.screens.trainers.TrainerItemUi

@Composable
fun TrainersListSection(
    trainers: List<TrainerItemUi>,
    onTrainerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (trainers.isEmpty()) return

    Column(modifier = modifier) {
        val lastIndex = trainers.lastIndex
        trainers.forEachIndexed { index, trainer ->
            TrainerCard(
                trainer = trainer,
                onClick = { onTrainerClick(trainer.id) }
            )
            if (index != lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(29.dp))
    }
}
