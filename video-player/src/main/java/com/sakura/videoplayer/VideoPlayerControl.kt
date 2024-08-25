package com.sakura.videoplayer


import android.content.res.Configuration
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.imherrera.videoplayer.icons.Fullscreen
import com.imherrera.videoplayer.icons.FullscreenExit
import com.sakura.video_player.R
import com.sakura.videoplayer.component.Slider
import com.sakura.videoplayer.icons.ArrowBackIos
import com.sakura.videoplayer.icons.Pause
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VideoPlayerControl(
    state: VideoPlayerState,
    title: String,
    subtitle: String? = null,
    background: Color = Color.Black.copy(0.2f),
    contentColor: Color = Color.LightGray,
    progressLineColor: Color = MaterialTheme.colorScheme.inversePrimary,
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    optionsContent: (@Composable () -> Unit)? = null,
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(
                    start = 8.dp + if (
                        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    ) {
                        WindowInsets.displayCutout
                            .asPaddingValues()
                            .calculateLeftPadding(LayoutDirection.Ltr)
                    } else 0.dp, end = 28.dp, top = 18.dp
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                ControlHeader(
                    modifier = Modifier.fillMaxWidth(),
                    title = title,
                    subtitle = subtitle,
                    isSeeking = state.isSeeking.value,
                    optionsContent = optionsContent,
                    onBackClick = onBackClick,
                )

                Spacer(Modifier.size(1.dp))

                TimelineControl(
                    modifier = Modifier.fillMaxWidth(),
                    progressLineColor = progressLineColor,
                    isFullScreen = state.isFullscreen.value,
                    videoDurationMs = state.videoDurationMs.value,
                    videoPositionMs = state.videoPositionMs.value,
                    videoProgress = state.videoProgress.value,
                    videoBufferedProgress = state.videoBufferedProgress.value,
                    control = state.control,
                    isSeeking = state.isSeeking.value,
                    isPlaying = state.isPlaying.value,
                    onFullScreenToggle = { state.control.setFullscreen(!state.isFullscreen.value) },
                    onClickSlider = { state.onClickSlider(it) },
                    onDragSlider = { state.onSeeking(it) },
                    onDragSliderFinished = { state.onSeeked() },
                    speedText = state.speedText.value,
                    resizeText = state.resizeText.value,
                    onSpeedClick = { state.showSpeedUi() },
                    onResizeClick = { state.showResizeUi() },
                    onEpisodeClick = { state.showEpisodeUi() },
                    onNextClick = onNextClick
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
    isSeeking: Boolean,
    onBackClick: (() -> Unit)?,
    optionsContent: (@Composable () -> Unit)? = null,
) {
    if (isSeeking) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(modifier = Modifier
            .size(BigIconButtonSize),
            onClick = { onBackClick?.invoke() }) {
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

        optionsContent?.invoke()

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
    videoBufferedProgress: Float,
    isSeeking: Boolean,
    isPlaying: Boolean,
    control: VideoPlayerControl,
    onClickSlider: (Float) -> Unit,
    onDragSlider: (Float) -> Unit,
    onDragSliderFinished: () -> Unit,
    onFullScreenToggle: () -> Unit,
    speedText: String,
    resizeText: String,
    onSpeedClick: () -> Unit,
    onResizeClick: () -> Unit,
    onEpisodeClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    val timestamp = remember(videoDurationMs, videoPositionMs.milliseconds.inWholeSeconds) {
        prettyVideoTimestamp(videoPositionMs.milliseconds, videoDurationMs.milliseconds)
    }

    Column(
        modifier = modifier
    ) {
        if (!isSeeking) {
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
        }

        Slider(
            value = if (videoProgress.isNaN()) 0f else videoProgress,
            secondValue = if (videoBufferedProgress.isNaN()) 0f else videoBufferedProgress,
            onClick = onClickSlider,
            onValueChange = onDragSlider,
            onValueChangeFinished = onDragSliderFinished,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            isSeeking = isSeeking,
            color = progressLineColor,
        )

        if (isSeeking) {
            Spacer(modifier = Modifier.size(MediumIconButtonSize))
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AdaptiveIconButton(
                        modifier = Modifier.size(MediumIconButtonSize),
                        onClick = { if (isPlaying) control.pause() else control.play() }
                    ) {
                        Icon(
                            modifier = Modifier.fillMaxSize(),
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = null
                        )
                    }

                    AdaptiveIconButton(
                        modifier = Modifier.size(MediumIconButtonSize),
                        onClick = onNextClick
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_next),
                            contentDescription = null
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                    AdaptiveTextButton(
                        text = "选集",
                        modifier = Modifier.size(MediumIconButtonSize),
                        onClick = onEpisodeClick
                    )

                    AdaptiveTextButton(
                        text = speedText,
                        modifier = Modifier.size(MediumIconButtonSize),
                        onClick = onSpeedClick
                    )

                    AdaptiveTextButton(
                        text = resizeText,
                        modifier = Modifier.size(MediumIconButtonSize),
                        onClick = onResizeClick
                    )

                }

            }

        }
    }
}

@Composable
fun AdaptiveTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    color: Color = LocalContentColor.current,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    AdaptiveIconButton(
        modifier = modifier,
        enabledIndication = false,
        onClick = onClick
    ) {
        Text(
            text = text,
            color = color,
            style = style,
        )
    }
}

@Composable
private fun AdaptiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabledIndication: Boolean = true,
    content: @Composable () -> Unit
) {
    val indication = LocalIndication.current

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = if (enabledIndication) indication else null
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private val BigIconButtonSize = 52.dp
private val MediumIconButtonSize = 42.dp
private val SmallIconButtonSize = 32.dp