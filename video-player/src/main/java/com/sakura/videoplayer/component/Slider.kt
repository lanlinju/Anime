package com.sakura.videoplayer.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.delay

@Composable
fun Slider(
    value: Float,
    secondValue: Float,
    modifier: Modifier = Modifier,
    onClick: (Float) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = Color.LightGray.copy(alpha = 0.38f),
    secondTrackColor: Color =Color.LightGray.copy(alpha = 0.78f),
    isSeeking: Boolean = false
) {
    val isAnimHeight = remember(isSeeking) { mutableStateOf(isSeeking) }
    val animHeight = animateDpAsState(
        targetValue = if (isAnimHeight.value) 4.dp else 2.dp,
        animationSpec = tween()
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { offset ->
                    isAnimHeight.value = true
                    onClick(offset.x / size.width)
                }, onPress = {
                    tryAwaitRelease()
                    delay(150)
                    isAnimHeight.value = false
                })
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        onValueChange((change.position.x / size.width).coerceIn(0f, 1f))
                        change.consume()
                    },
                    onDragEnd = onValueChangeFinished
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {

        // track
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

        // 视频缓冲进度
        Box(
            modifier = Modifier
                .fillMaxWidth(secondValue)
                .height(animHeight.value)
                .background(
                    color = secondTrackColor,
                    shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp)
                )
        )

        // 视频播放进度
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

        // thumb
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .align(
                    BiasAlignment(
                        horizontalBias = (value * 2) - 1f, // -1 start | 0 center | 1 end
                        verticalBias = 0f
                    )
                )
                .size(15.dp)
                .background(color)
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