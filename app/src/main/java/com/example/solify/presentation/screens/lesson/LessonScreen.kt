package com.example.solify.presentation.screens.lesson

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LessonScreen(
    lessonId: String,
    onNavigateBack: () -> Unit
) {

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                "Back",
                Modifier.clickable{
                    onNavigateBack()
                }
            )
            Spacer(Modifier.padding(40.dp))
            Text(
                lessonId
            )
        }
    }
}