package com.example.solify.presentation.screens.theory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.solify.R
import com.example.solify.presentation.screens.theory.TheoryAudioUiState
import com.example.solify.presentation.ui.theme.Brown100
import com.example.solify.presentation.ui.theme.Brown300
import com.example.solify.presentation.ui.theme.White300
import com.example.solify.presentation.ui.theme.Yellow200
import com.example.solify.presentation.utils.TheoryTextFormatter

@Composable
fun TheoryTextBlock(
    text: String,
    modifier: Modifier = Modifier
) {
    val annotatedText = TheoryTextFormatter.parse(
        text = text,
        accentColor = MaterialTheme.colorScheme.secondary
    )

    Text(
        text = annotatedText,
        style = MaterialTheme.typography.displayMedium.copy(fontStyle = FontStyle.Italic),
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    )
}

@Composable
fun TheoryImageBlock(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .width(308.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
fun TheoryAudioBlock(
    audioUrl: String,
    audioState: TheoryAudioUiState,
    onPlayToggle: () -> Unit,
    onMuteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = audioState.activeUrl == audioUrl
    val isPlaying = isActive && audioState.isPlaying
    val isLoading = isActive && audioState.isLoading

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(White300)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Yellow200)
                .clickable(enabled = !isLoading, onClick = onPlayToggle),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Brown300
                    )
                }

                isPlaying -> {
                    Icon(
                        painter = painterResource(R.drawable.pause),
                        contentDescription = null,
                        modifier = Modifier.size(10.5.dp),
                        tint = White300
                    )
                }

                else -> {
                    Icon(
                        painter = painterResource(R.drawable.play),
                        contentDescription = null,
                        modifier = Modifier.size(10.5.dp),
                        tint = White300
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Аудио",
                style = MaterialTheme.typography.bodyLarge,
                color = Brown300
            )

            TheoryAudioProgressBar(
                progress = if (isActive) audioState.progress else 0f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Icon(
            painter = if (audioState.isMuted) {
                painterResource(R.drawable.volume_off)
            } else {
                painterResource(R.drawable.volume)
            },
            contentDescription = null,
            modifier = Modifier
                .width(if (audioState.isMuted) 20.dp else 17.dp)
                .clickable(onClick = onMuteToggle),
            tint = Brown300
        )
    }
}

@Composable
private fun TheoryAudioProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Brown100)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(2.dp))
                .background(Yellow200)
        )
    }
}
