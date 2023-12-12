package com.sakura.anime.presentation.screen.videoplay

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura.anime.R
import com.sakura.anime.presentation.component.LoadingIndicator
import com.sakura.anime.presentation.component.StateHandler
import com.sakura.anime.presentation.component.WarningMessage
import com.sakura.video_player.VideoPlayer
import com.sakura.video_player.VideoPlayerControl
import com.sakura.video_player.VideoPlayerState
import com.sakura.video_player.prettyVideoTimestamp
import com.sakura.video_player.rememberVideoPlayerState
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VideoPlayScreen(
    viewModel: VideoPlayViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    activity: Activity
) {
    val animeVideoUrlState by viewModel.videoUrlState.collectAsState()

    StateHandler(
        state = animeVideoUrlState,
        onLoading = { LoadingIndicator() },
        onFailure = { WarningMessage(textId = R.string.txt_empty_result) }
    ) { resource ->
        resource.data?.let { videoUrl ->
            val playerState = rememberVideoPlayerState()

            val localView = LocalView.current
            localView.keepScreenOn = true

            activity.requestedOrientation =
                if (playerState.isFullscreen.value) {
                    hideSystemBars(LocalView.current)
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    showSystemBars(LocalView.current)
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                VideoPlayer(
                    url = videoUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .adaptiveSize(playerState.isFullscreen.value, localView),
                    playerState = playerState,
                    onBackPress = {
                        playerState.control.setFullscreen(false)
                        onBackClick()
                    }
                ) {
                    VideoPlayerControl(
                        state = playerState,
                        title = viewModel.animeTitle,
                        onBackClick = {
                            playerState.control.setFullscreen(false)
                            onBackClick()
                        }
                    )
                }

                VideoStateMessage(playerState)

                VolumeBrightnessIndicator(playerState)

                DisposableEffect(localView) {
                    onDispose {
                        localView.keepScreenOn = false
                    }
                }
            }

        }

    }

}

@Composable
fun VideoStateMessage(playerState: VideoPlayerState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (playerState.isLoading.value && !playerState.isError.value && !playerState.isSeeking.value) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        if (playerState.isError.value) {
            ShowVideoMessage(stringResource(id = R.string.video_error_msg))
        }

        if (playerState.isEnded.value) {
            ShowVideoMessage(stringResource(id = R.string.video_ended_msg))
        }

        if (playerState.isSeeking.value) {
            TimelineIndicator(
                (playerState.videoDurationMs.value * playerState.videoProgress.value).toLong(),
                playerState.videoDurationMs.value
            )
        }

        if (playerState.isLongPress.value) {
            FastForwardIndicator(Modifier.align(Alignment.TopCenter))
        }

    }
}

@Composable
fun FastForwardIndicator(modifier: Modifier) {
    Box(
        modifier = modifier
            .padding(top = dimensionResource(id = R.dimen.medium_padding))
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(0.35f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.small_padding)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FastForwardAnimation()

            Text(
                text = stringResource(id = R.string.fast_forward_2x),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.offset(-12.dp)
            )
        }

    }
}

@Composable
fun FastForwardAnimation(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()

    Row(modifier) {
        repeat(3) { index ->
            val color by transition.animateColor(
                initialValue = Color.LightGray.copy(alpha = 0.1f),
                targetValue = Color.LightGray,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 250)
                )
            )

            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "",
                modifier = Modifier.offset(-(index * 12).dp),
                tint = color
            )
        }
    }
}

@Composable
fun VolumeBrightnessIndicator(playerState: VideoPlayerState, modifier: Modifier = Modifier) {
    if (playerState.isChangingBrightness.value || playerState.isChangingVolume.value) {
        Box(
            modifier = modifier
                .width(200.dp)
                .aspectRatio(3.5f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.medium_padding)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.medium_padding))
            ) {
                if (playerState.isChangingBrightness.value) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(id = R.drawable.ic_brightness),
                        tint = Color.White,
                        contentDescription = stringResource(id = R.string.brightness)
                    )
                } else {
                    if (playerState.volumeBrightnessProgress.value == 0f) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            painter = painterResource(id = R.drawable.ic_volume_mute),
                            tint = Color.White,
                            contentDescription = stringResource(id = R.string.brightness)
                        )
                    } else {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            painter = painterResource(id = R.drawable.ic_volume_up),
                            tint = Color.White,
                            contentDescription = stringResource(id = R.string.brightness)
                        )
                    }
                }

                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.medium_padding))
                        .height(2.dp),
                    progress = playerState.volumeBrightnessProgress.value,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}


@Composable
fun TimelineIndicator(
    videoPositionMs: Long,
    videoDurationMs: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(120.dp)
            .aspectRatio(2.5f)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = prettyVideoTimestamp(
                videoPositionMs.milliseconds,
                videoDurationMs.milliseconds
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

private fun Modifier.adaptiveSize(fullscreen: Boolean, view: View): Modifier {
    return if (fullscreen) {
        hideSystemBars(view)
        fillMaxSize()
    } else {
        showSystemBars(view)
        fillMaxWidth().aspectRatio(1.778f)
    }
}

@Composable
private fun ShowVideoMessage(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = CircleShape
    ) {
        Text(
            modifier = Modifier.padding(12.dp),
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun hideSystemBars(view: View) {
    val windowInsetsController = ViewCompat.getWindowInsetsController(view) ?: return
    // Configure the behavior of the hidden system bars
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    // Hide both the status bar and the navigation bar
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
}

private fun showSystemBars(view: View) {
    val windowInsetsController = ViewCompat.getWindowInsetsController(view) ?: return
    // Show both the status bar and the navigation bar
    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
}


