package com.example.solify.presentation.components.skeleton

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.solify.presentation.ui.theme.Grey100
import com.example.solify.presentation.ui.theme.Grey200
import com.example.solify.presentation.ui.theme.White100
import com.example.solify.presentation.ui.theme.White200

enum class SkeletonSurface {
    ON_PRIMARY,
    ON_PRIMARY_CONTAINER,
    ON_CARD
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    surface: SkeletonSurface = SkeletonSurface.ON_PRIMARY
) {
    val (baseColor, highlightColor) = skeletonColors(surface)
    Box(
        modifier = modifier
            .clip(shape)
            .skeletonShimmer(baseColor = baseColor, highlightColor = highlightColor)
    )
}

@Composable
fun SkeletonCircle(
    size: Dp,
    modifier: Modifier = Modifier,
    surface: SkeletonSurface = SkeletonSurface.ON_PRIMARY
) {
    SkeletonBox(
        modifier = modifier.size(size),
        shape = CircleShape,
        surface = surface
    )
}

@Composable
private fun skeletonColors(surface: SkeletonSurface): Pair<Color, Color> {
    return when (surface) {
        SkeletonSurface.ON_PRIMARY -> White100 to White200
        SkeletonSurface.ON_PRIMARY_CONTAINER -> Grey100 to Grey200
        SkeletonSurface.ON_CARD -> Grey200 to White100
    }
}

private fun Modifier.skeletonShimmer(
    baseColor: Color,
    highlightColor: Color
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeleton_shimmer_offset"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            highlightColor.copy(alpha = highlightColor.alpha * 0.85f),
            baseColor
        ),
        start = Offset(translate - 320f, translate - 320f),
        end = Offset(translate, translate)
    )
    background(brush)
}
