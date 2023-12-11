package com.sakura.video_player


import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.imherrera.videoplayer.icons.Fullscreen
import com.imherrera.videoplayer.icons.FullscreenExit
import com.sakura.video_player.component.Slider
import com.sakura.video_player.icons.ArrowBackIos
import com.sakura.video_player.icons.Pause
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VideoPlayerControl(
    state: VideoPlayerState,
    title: String,
    subtitle: String? = null,
    background: Color = Color.Black.copy(0.25f),
    contentColor: Color = Color.White,
    progressLineColor: Color = MaterialTheme.colorScheme.inversePrimary,
    onBackClick: () -> Unit = {},
    onOptionsContent: (@Composable () -> Unit)? = null,
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(start = 8.dp, end = 28.dp, top = 20.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                if (!state.isSeeking.value)
                    ControlHeader(
                        modifier = Modifier.fillMaxWidth(),
                        title = title,
                        subtitle = subtitle,
                        onOptionsContent = onOptionsContent,
                        onBackClick = onBackClick
                    )

                Spacer(Modifier.size(2.dp))

                TimelineControl(
                    modifier = Modifier.fillMaxWidth(),
                    progressLineColor = progressLineColor,
                    isFullScreen = state.isFullscreen.value,
                    videoDurationMs = state.videoDurationMs.value,
                    videoPositionMs = state.videoPositionMs.value,
                    videoProgress = state.videoProgress.value,
                    control = state.control,
                    isSeeking = state.isSeeking.value,
                    onFullScreenToggle = { state.control.setFullscreen(!state.isFullscreen.value) },
                    onDragSlider = { state.onSeeking(it) },
                    onDragSliderFinished = { state.onSeeked() },
                    isPlaying = state.isPlaying.value
                )
            }
        }

    }
}

@Composable
private fun ControlHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    onBackClick: (() -> Unit)?,
    onOptionsContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(modifier = Modifier
            .size(BigIconButtonSize)
            .padding(10.dp), onClick = { onBackClick?.invoke() }) {
            Icon(imageVector = Icons.Rounded.ArrowBackIos, contentDescription = null)
        }
        Column(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = LocalContentColor.current,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = LocalContentColor.current.copy(0.80f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        onOptionsContent?.invoke()
    }
}

@Composable
private fun TimelineControl(
    modifier: Modifier,
    progressLineColor: Color,
    isFullScreen: Boolean,
    videoDurationMs: Long,
    videoPositionMs: Long,
    videoProgress: Float,
    isSeeking: Boolean,
    isPlaying: Boolean,
    control: VideoPlayerControl,
    onDragSlider: (Float) -> Unit,
    onDragSliderFinished: () -> Unit,
    onFullScreenToggle: () -> Unit,
) {
    val timestamp = remember(videoDurationMs, videoPositionMs.milliseconds.inWholeSeconds) {
        prettyVideoTimestamp(videoPositionMs.milliseconds, videoDurationMs.milliseconds)
    }
    Column(
        modifier = modifier
    ) {
        if (!isSeeking)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = timestamp, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.weight(1.0f))
                AdaptiveIconButton(
                    modifier = Modifier.size(SmallIconButtonSize),
                    onClick = onFullScreenToggle
                ) {
                    Icon(
                        imageVector = if (isFullScreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                        contentDescription = null
                    )
                }

            }

        Slider(
            value = if (videoProgress.isNaN()) 0f else videoProgress,
            onValueChange = onDragSlider,
            onValueChangeFinished = onDragSliderFinished,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            isSeeking = isSeeking,
            color = progressLineColor,
            trackColor = Color.LightGray
        )
        if (isSeeking)
            Spacer(modifier = Modifier.size(42.dp))
        else
            AdaptiveIconButton(
                modifier = Modifier.size(42.dp),
                onClick = { if (isPlaying) control.pause() else control.play() }
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null
                )
            }
    }
}

@Composable
private fun AdaptiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private val BigIconButtonSize = 52.dp
private val SmallIconButtonSize = 32.dp

