package com.example.solify.presentation.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val AvatarProgressGap = 3.dp
private val AvatarProgressStrokeWidth = 3.dp

@Composable
fun AvatarWithProgressRing(
    completionPercent: Int,
    avatarSize: Int,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerSize = avatarSize.dp + (AvatarProgressGap + AvatarProgressStrokeWidth) * 2

    Box(
        modifier = modifier.size(containerSize),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressRing(
            percent = completionPercent,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = AvatarProgressStrokeWidth
        )
        AvatarImage(
            imageUrl = imageUrl,
            onClick = onClick,
            size = avatarSize,
            modifier = Modifier
                .size(avatarSize.dp)
                .clickable(onClick = onClick)
        )
    }
}
