package com.sakura.videoplayer.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: () -> Unit = {},
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
    isSeeking: Boolean = false
) {
    val animHeight = animateDpAsState(
        targetValue = if (isSeeking) 4.dp else 2.dp,
        animationSpec = tween()
    )
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = onValueChangeFinished
                ) { change, _ ->
                    onValueChange((change.position.x / size.width).coerceIn(0f, 1f))
                    change.consume()
                }

            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(
                    FractionClip(fraction = value, start = true)
                )
                .fillMaxWidth()
                .height(animHeight.value)
                .background(
                    color = color,
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Box(
            modifier = Modifier
                .clip(
                    FractionClip(fraction = value, start = false)
                )
                .fillMaxWidth()
                .height(animHeight.value)
                .background(
                    color = trackColor,
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .align(
                    BiasAlignment(
                        horizontalBias = (value * 2) - 1f,
                        verticalBias = 0f
                    )
                )
                .size(15.dp)
                .background(color = color, shape = CircleShape)
        )
    }
}

class FractionClip(val fraction: Float, val start: Boolean) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            rect = Rect(
                left = when (start) {
                    true -> 0f
                    false -> size.width * fraction
                },
                top = 0f,
                right = when (start) {
                    true -> size.width * fraction
                    false -> size.width
                },
                bottom = size.height,
            )
        )
    }
}