package com.example.solify.presentation.screens.test.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.solify.presentation.screens.test.AnswerOptionVisualState
import com.example.solify.presentation.ui.theme.Green100
import com.example.solify.presentation.ui.theme.LightYellow300
import com.example.solify.presentation.ui.theme.Red200

@Composable
fun TestAnswerOption(
    text: String,
    visualState: AnswerOptionVisualState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(15.dp)
    val backgroundColor: Color
    val borderColor: Color?
    val textColor: Color

    when (visualState) {
        AnswerOptionVisualState.DEFAULT -> {
            backgroundColor = Color.Transparent
            borderColor = MaterialTheme.colorScheme.onPrimary
            textColor = MaterialTheme.colorScheme.onPrimary
        }

        AnswerOptionVisualState.SELECTED -> {
            backgroundColor = MaterialTheme.colorScheme.secondary
            borderColor = null
            textColor = LightYellow300
        }

        AnswerOptionVisualState.CORRECT,
        AnswerOptionVisualState.CORRECT_REVEALED -> {
            backgroundColor = Green100
            borderColor = null
            textColor = LightYellow300
        }

        AnswerOptionVisualState.INCORRECT -> {
            backgroundColor = Red200
            borderColor = null
            textColor = LightYellow300
        }
    }

    var optionModifier = modifier
        .widthIn(min = 221.dp, max = 280.dp)
        .defaultMinSize(minHeight = 38.dp)
        .clip(shape)

    optionModifier = if (borderColor != null) {
        optionModifier.border(width = 1.dp, color = borderColor, shape = shape)
    } else {
        optionModifier
    }

    optionModifier = optionModifier
        .background(backgroundColor)
        .then(
            if (enabled) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        )

    Box(
        modifier = optionModifier.padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
