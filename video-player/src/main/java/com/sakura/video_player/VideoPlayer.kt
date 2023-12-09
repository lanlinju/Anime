package com.sakura.video_player


import android.media.AudioManager
import android.provider.Settings
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.MediaItem
import kotlin.math.abs

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

private fun Modifier.defaultPlayerDragGestures(playerState: VideoPlayerState) =
    pointerInput(Unit) {
        var downX = 0f
        var downY = 0f
        var isChangePosition = false
        var isChangeVolume = false
        var isChangeBrightness = false
        var gestureDownPosition = 0L
        var gestureDownVolume = 0
        var gestureDownBrightness = 0f
        val maxFastForwardDuration = 100_000L // 最大快进时长为100秒
        val threshold = (12.dp).toPx() // 水平距离超过12dp后开始计算
        val audioManager = playerState.audioManager
        val layoutParams = playerState.window.attributes
        detectDragGestures(
            onDragStart = { offset ->
                downX = offset.x
                downY = offset.y
                isChangePosition = false
                isChangeVolume = false
                isChangeBrightness = false
            }, onDragEnd = {
                if (isChangePosition) playerState.onSeeked()
                playerState.onChanged()
            }) { change, dragAmount ->
            val deltaX = change.position.x - downX
            var deltaY = change.position.y - downY
            val absDeltaX = abs(deltaX)
            val absDeltaY = abs(deltaY)
            if (!isChangePosition && !isChangeVolume && !isChangeBrightness
                && downY > size.height * 0.15f && downY < size.height * 0.9f
            ) {
                if (absDeltaX > threshold || absDeltaY > threshold) {
                    if (absDeltaX >= threshold) {
                        isChangePosition = true
                        gestureDownPosition = playerState.player.currentPosition
                    } else {
                        if (change.position.x < size.width * 0.5f) {
                            isChangeBrightness = true
                            if (layoutParams.screenBrightness < 0f) {
                                gestureDownBrightness = Settings.System.getInt(
                                    playerState.window.context.contentResolver,
                                    Settings.System.SCREEN_BRIGHTNESS
                                ).toFloat()
                            } else {
                                gestureDownBrightness = layoutParams.screenBrightness * 255
                            }
                        } else {
                            isChangeVolume = true
                            gestureDownVolume =
                                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        }
                    }
                }
            }
            if (isChangePosition && playerState.videoDurationMs.value > 0) {
                val deltaMs = deltaX / size.width * maxFastForwardDuration
                val seekTimePosition = gestureDownPosition + deltaMs
                val progress =
                    (seekTimePosition / playerState.videoDurationMs.value).coerceIn(0f..1f)
                playerState.onSeeking(progress)
            }
            if (isChangeVolume) {
                deltaY = -deltaY
                val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val deltaV = (max * deltaY * 3 / size.height).toInt()
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC, gestureDownVolume + deltaV, 0
                )
                val volumePercent = (gestureDownVolume + deltaV) / max.toFloat()
                playerState.onChangeVolume(volumePercent.coerceIn(0f..1f))
            }
            if (isChangeBrightness) {
                deltaY = -deltaY
                val deltaV = 255 * deltaY * 3 / size.height
                val brightnessPercent = ((gestureDownBrightness + deltaV) / 255)
                layoutParams.screenBrightness = brightnessPercent.coerceIn(0.01f..1f)
                playerState.window.attributes = layoutParams
                playerState.onChangeBrightness(brightnessPercent.coerceIn(0f..1f))
            }
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
            .defaultPlayerDragGestures(playerState)
    ) {

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
    onBackPress: () -> Unit,
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
            playerState.abandonAudioFocus()
            lifecycleOwner.lifecycle.removeObserver(observer)
            playerState.player.release()
        }
    }

    LaunchedEffect(url) {
        playerState.player.setMediaItem(MediaItem.fromUri(url))
        playerState.player.prepare()
        playerState.player.playWhenReady = true
        playerState.requestAudioFocus()
    }

    BackHandler {
        onBackPress()
    }
}
