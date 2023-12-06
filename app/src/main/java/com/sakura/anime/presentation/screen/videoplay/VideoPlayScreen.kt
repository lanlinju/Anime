package com.sakura.anime.presentation.screen.videoplay

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura.anime.presentation.component.LoadingIndicator
import com.sakura.anime.presentation.component.StateHandler
import com.sakura.anime.presentation.component.WarningMessage
import com.sakura.anime.R
import com.sakura.video_player.VideoPlayer
import com.sakura.video_player.VideoPlayerControl
import com.sakura.video_player.VideoPlayerState
import com.sakura.video_player.prettyVideoTimestamp
import com.sakura.video_player.rememberVideoPlayerState
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VideoPlayScreen(
    viewModel: VideoPlayViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    activity: Activity
) {
    val animeVideoUrlState by viewModel.videoUrlState.collectAsState()
    val localView = LocalView.current
    localView.keepScreenOn = true

    StateHandler(
        state = animeVideoUrlState,
        onLoading = { LoadingIndicator() },
        onFailure = { WarningMessage(textId = R.string.txt_empty_result) }
    ) { resource ->
        resource.data?.let { videoUrl ->
            val playerState = rememberVideoPlayerState()
            activity.requestedOrientation =
                if (playerState.isFullscreen.value) {
                    hideSystemBars(LocalView.current)
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    showSystemBars(LocalView.current)
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

            Box(
                contentAlignment = Alignment.Center, modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                VideoPlayer(
                    url = videoUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .defaultPlayerDragGestures(playerState)
                        .adaptiveSize(playerState.isFullscreen.value, localView),
                    playerState = playerState,
                    onBackPress = {
                        playerState.control.setFullscreen(false)
                        onBackClick()
                    }
                ) {
                    VideoPlayerControl(
                        state = playerState,
                        title = "",
                        onBackClick = {
                            playerState.control.setFullscreen(false)
                            onBackClick()
                        }
                    )
                }

                VideoStateMessage(playerState)

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

private fun Modifier.defaultPlayerDragGestures(playerState: VideoPlayerState) =
    pointerInput(Unit) {
        var totalDragX = 0f
        var isSeek = false
        val maxFastForwardDuration = 100_000L // 最大快进时长为100秒
        val threshold = 12.dp // 水平距离超过12dp后开始计算
        detectDragGestures(onDragEnd = {
            if (isSeek) playerState.onSeeked()
            totalDragX = 0f
            isSeek = false
        }) { _, dragAmount ->
            if (playerState.videoDurationMs.value > 0L && abs(dragAmount.x) > 3 * abs(dragAmount.y)) {
                totalDragX += dragAmount.x
                if (abs(totalDragX) > threshold.toPx()) {
                    val deltaMs = dragAmount.x / size.width * maxFastForwardDuration
                    playerState.onSeeking(
                        (playerState.videoProgress.value + (deltaMs / playerState.videoDurationMs.value)).coerceIn(
                            0F..1F
                        )
                    )
                    isSeek = true
                }
            }
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


