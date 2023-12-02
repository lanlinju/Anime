package com.sakura.video_player


import android.view.SurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.MediaItem


private fun Modifier.adaptiveLayout(
    aspectRatio: Float,
) = layout { measurable, constraints ->
    val resizedConstraint = constraints.resizeForVideo(aspectRatio)
    val placeable = measurable.measure(resizedConstraint)
    layout(constraints.maxWidth, constraints.maxHeight) {
        // Center x and y axis relative to the layout
        placeable.placeRelative(
            x = (constraints.maxWidth - resizedConstraint.maxWidth) / 2,
            y = (constraints.maxHeight - resizedConstraint.maxHeight) / 2
        )
    }
}

private fun Modifier.defaultPlayerTapGestures(playerState: VideoPlayerState) =
    pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = {
                if (playerState.isPlaying.value) {
                    playerState.control.pause()
                } else {
                    playerState.control.play()
                }
            },
            onTap = {
                if (playerState.isControlUiVisible.value) {
                    playerState.hideControlUi()
                } else {
                    playerState.showControlUi()
                }
            }
        )
    }

@Composable
private fun VideoPlayer(
    modifier: Modifier,
    playerState: VideoPlayerState,
    controller: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultPlayerTapGestures(playerState)
    ) {
        BackHandler(enabled = playerState.isFullscreen.value) {
            playerState.control.setFullscreen(false)
        }

        AndroidView(
            modifier = Modifier
                .adaptiveLayout(
                    aspectRatio = playerState.videoSize.value.aspectRatio(),
                ),
            factory = { context ->
                SurfaceView(context).also {
                    playerState.player.setVideoSurfaceView(it)
                }
            }
        )

        AnimatedVisibility(
            visible = playerState.isControlUiVisible.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            controller()
        }
    }
}

/**
 * @param playerState state to attach to this composable.
 * @param lifecycleOwner required to manage the ExoPlayer instance.
 * @param controller you can use [VideoPlayerControl] or alternatively implement your own
 * */
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    url: String,
    playerState: VideoPlayerState,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    controller: @Composable () -> Unit,
) {
    VideoPlayer(
        modifier = modifier,
        playerState = playerState,
        controller = controller
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> playerState.showControlUi()
                Lifecycle.Event.ON_STOP -> playerState.player.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            playerState.player.release()
        }
    }

    LaunchedEffect(url) {
        playerState.player.setMediaItem(MediaItem.fromUri(url))
        playerState.player.prepare()
        playerState.player.playWhenReady = true
    }
}
