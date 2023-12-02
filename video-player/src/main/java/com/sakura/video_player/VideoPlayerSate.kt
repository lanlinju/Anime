package com.sakura.video_player

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.video.VideoSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberVideoPlayerState(
    hideControllerAfterMs: Long = 6000,
    videoPositionPollInterval: Long = 500,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    config: ExoPlayer.Builder.() -> Unit = { }
): VideoPlayerState = remember {
    VideoPlayerStateImpl(
        player = ExoPlayer.Builder(context).apply(config).build(),
        coroutineScope = coroutineScope,
        hideControllerAfterMs = hideControllerAfterMs,
        videoPositionPollInterval = videoPositionPollInterval
    ).also {
        it.player.addListener(it)
    }
}

class VideoPlayerStateImpl(
    override val player: ExoPlayer,
    private val coroutineScope: CoroutineScope,
    private val hideControllerAfterMs: Long,
    private val videoPositionPollInterval: Long,
) : VideoPlayerState, Player.Listener {
    override val videoSize = mutableStateOf(player.videoSize)
    override val videoPositionMs = mutableStateOf(0L)
    override val videoDurationMs = mutableStateOf(0L)

    override val isFullscreen = mutableStateOf(true)
    override val isPlaying = mutableStateOf(player.isPlaying)
    override val isLoading = mutableStateOf(true)
    override val isEnded = mutableStateOf(false)
    override val isError = mutableStateOf(false)
    override val playerState = mutableStateOf(player.playbackState)

    override val isSeeking = mutableStateOf(false)
    override val videoProgress = mutableStateOf(0F)

    override val onSeeking: (Float) -> Unit
        get() = {
            controlUiLastInteractionMs = 0
            isSeeking.value = true
            isEnded.value = false
            if (!isControlUiVisible.value) {
                showControlUi()
            }
            this.videoProgress.value = it
            if (videoDurationMs.value > 0) {
                videoPositionMs.value = player.currentPosition
                player.seekTo((player.duration * videoProgress.value).toLong())
            }
        }
    override val onSeeked: (endDragProcess: Float) -> Unit
        get() = {
            isSeeking.value = false
        }

    override val isControlUiVisible = mutableStateOf(false)
    override val control = object : VideoPlayerControl {
        override fun play() {
            controlUiLastInteractionMs = 0
            player.play()
        }

        override fun pause() {
            controlUiLastInteractionMs = 0
            player.pause()
        }

        override fun setFullscreen(value: Boolean) {
            controlUiLastInteractionMs = 0
            isFullscreen.value = value
        }
    }

    private var pollVideoPositionJob: Job? = null
    private var controlUiLastInteractionMs = 0L

    override fun hideControlUi() {
        controlUiLastInteractionMs = 0
        isControlUiVisible.value = false
        pollVideoPositionJob?.cancel()
        pollVideoPositionJob = null
    }

    override fun showControlUi() {
        isControlUiVisible.value = true
        pollVideoPositionJob?.cancel()
        pollVideoPositionJob = coroutineScope.launch {
            while (true) {
                if (!isSeeking.value) {
                    if (videoDurationMs.value > 0) {
                        videoPositionMs.value = player.currentPosition
                        videoProgress.value =
                            videoPositionMs.value / videoDurationMs.value.toFloat()
                    }
                    controlUiLastInteractionMs += videoPositionPollInterval

                }
                delay(videoPositionPollInterval)
                if (controlUiLastInteractionMs >= hideControllerAfterMs) {
                    hideControlUi()
                    break
                }
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        this.isPlaying.value = isPlaying
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) videoDurationMs.value = player.duration
        this.playerState.value = playbackState
        when (playbackState) {
            Player.STATE_IDLE -> Unit
            Player.STATE_BUFFERING -> isLoading.value = true
            Player.STATE_READY -> isLoading.value = false
            Player.STATE_ENDED -> isEnded.value = true
        }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        this.videoSize.value = videoSize
    }

    override fun onPlayerError(error: PlaybackException) {
        isError.value = true
    }
}


interface VideoPlayerState {
    val player: ExoPlayer

    val videoSize: State<VideoSize>
    val videoPositionMs: State<Long>
    val videoDurationMs: State<Long>

    val isFullscreen: State<Boolean>
    val isPlaying: State<Boolean>
    val isLoading: State<Boolean>
    val isEnded: State<Boolean>
    val isError: State<Boolean>
    val playerState: State<Int>

    val isSeeking: State<Boolean>
    val videoProgress: State<Float> /*0f - 1f*/

    val onSeeking: (dragProcess: Float) -> Unit
    val onSeeked: (endDragProcess: Float) -> Unit

    val isControlUiVisible: State<Boolean>
    val control: VideoPlayerControl

    fun hideControlUi()
    fun showControlUi()
}

interface VideoPlayerControl {
    fun play()
    fun pause()

    fun setFullscreen(value: Boolean)
}