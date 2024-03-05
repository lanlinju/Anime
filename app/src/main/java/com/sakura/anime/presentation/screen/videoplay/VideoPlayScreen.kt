package com.sakura.anime.presentation.screen.videoplay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.componentsui.anime.domain.model.Episode
import com.sakura.anime.R
import com.sakura.anime.domain.model.Video
import com.sakura.anime.presentation.component.StateHandler
import com.sakura.anime.presentation.theme.AnimeTheme
import com.sakura.videoplayer.AdaptiveTextButton
import com.sakura.videoplayer.ResizeMode
import com.sakura.videoplayer.VideoPlayer
import com.sakura.videoplayer.VideoPlayerControl
import com.sakura.videoplayer.VideoPlayerState
import com.sakura.videoplayer.prettyVideoTimestamp
import com.sakura.videoplayer.rememberVideoPlayerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val Speeds = arrayOf(
    "0.5X" to 0.5f,
    "0.75X" to 0.75f,
    "1.0X" to 1.0f,
    "1.25X" to 1.25f,
    "1.5X" to 1.5f,
    "2.0X" to 2.0f
)

private val Resizes = arrayOf(
    "适应" to ResizeMode.Fit,
    "拉伸" to ResizeMode.Fill,
    "填充" to ResizeMode.Full,
    "16:9" to ResizeMode.FixedRatio_16_9,
    "4:3" to ResizeMode.FixedRatio_4_3,
)

/* 屏幕方向改变会导致丢失状态 */
@Composable
fun VideoPlayScreen(
    viewModel: VideoPlayViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    activity: Activity
) {
    val animeVideoState by viewModel.videoState.collectAsState()

    val view = LocalView.current

    StateHandler(
        state = animeVideoState,
        onLoading = {
            view.keepScreenOn = true
            requestLandscapeOrientation(view, activity)
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        onFailure = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    tint = Color.White,
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = stringResource(id = R.string.txt_empty_result),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                OutlinedButton(onClick = onBackClick) {
                    Text(text = stringResource(id = R.string.back), color = Color.White)
                }
            }
        }
    ) { resource ->
        resource.data?.let { video ->
            val playerState = rememberVideoPlayerState()

            val onBackHandle: () -> Unit = remember {
                {
                    playerState.control.setFullscreen(false)
                    requestPortraitOrientation(view, activity)
                    onBackClick()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .adaptiveSize(playerState.isFullscreen.value, view, activity),
                contentAlignment = Alignment.Center
            ) {

                VideoPlayer(
                    url = video.url,
                    playerState = playerState,
                    onBackPress = onBackHandle
                ) {
                    VideoPlayerControl(
                        state = playerState,
                        title = "${video.title}-${video.episodeName}",
                        onBackClick = onBackHandle
                    )
                }

                VideoStateMessage(playerState)

                VolumeBrightnessIndicator(playerState)

                VideoSideSheet(video, playerState, viewModel)
            }

        }
    }

    DisposableEffect(Unit) {
        onDispose {
            view.keepScreenOn = false
            requestPortraitOrientation(view, activity)
        }
    }
}

@SuppressLint("SourceLockedOrientationActivity")
private fun requestPortraitOrientation(view: View, activity: Activity) {
    showSystemBars(view, activity)
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

private fun requestLandscapeOrientation(view: View, activity: Activity) {
    hideSystemBars(view, activity)
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

private fun Modifier.adaptiveSize(fullscreen: Boolean, view: View, activity: Activity): Modifier {
    return if (fullscreen) {
        requestLandscapeOrientation(view, activity)
        fillMaxSize()
    } else {
        requestPortraitOrientation(view, activity)
        fillMaxWidth().aspectRatio(1.778f)
    }
}

private fun hideSystemBars(view: View, activity: Activity) {
    val windowInsetsController = WindowCompat.getInsetsController(activity.window, view)
    // Configure the behavior of the hidden system bars
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    // Hide both the status bar and the navigation bar
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
}

private fun showSystemBars(view: View, activity: Activity) {
    val windowInsetsController = WindowCompat.getInsetsController(activity.window, view)
    // Show both the status bar and the navigation bar
    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
}

@Composable
private fun VideoStateMessage(playerState: VideoPlayerState, modifier: Modifier = Modifier) {
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
private fun FastForwardIndicator(modifier: Modifier) {
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
private fun FastForwardAnimation(modifier: Modifier = Modifier) {
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
private fun VolumeBrightnessIndicator(
    playerState: VideoPlayerState,
    modifier: Modifier = Modifier
) {
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
private fun TimelineIndicator(
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

@Composable
private fun VideoSideSheet(
    video: Video,
    playerState: VideoPlayerState,
    viewModel: VideoPlayViewModel
) {
    var selectedSpeedIndex by remember { mutableIntStateOf(3) }
    var selectedResizeIndex by remember { mutableIntStateOf(0) }
    var selectedEpisodeIndex by remember { mutableIntStateOf(video.currentEpisodeIndex) }

    if (playerState.isSpeedUiVisible.value) {
        SpeedSideSheet(selectedSpeedIndex,
            onSpeedClick = { index, (speedText, speed) ->
                selectedSpeedIndex = index
                playerState.setSpeedText(if (index == 3) "倍速" else speedText)
                playerState.control.setPlaybackSpeed(speed)
            }, onDismissRequest = { playerState.hideSpeedUi() }
        )
    }

    if (playerState.isResizeUiVisible.value) {
        ResizeSideSheet(
            selectedResizeIndex = selectedResizeIndex,
            onResizeClick = { index, (resizeText, resizeMode) ->
                selectedResizeIndex = index
                playerState.setResizeText(resizeText)
                playerState.control.setVideoResize(resizeMode)
            }, onDismissRequest = { playerState.hideResizeUi() })
    }

    if (playerState.isEpisodeUiVisible.value) {
        EpisodeSideSheet(
            episodes = video.episodes,
            selectedEpisodeIndex = selectedEpisodeIndex,
            onEpisodeClick = { index, episode ->
                selectedEpisodeIndex = index
                viewModel.getVideo(episode.url, episode.name)
            },
            onDismissRequest = { playerState.hideEpisodeUi() }
        )
    }
}

@Composable
private fun SpeedSideSheet(
    selectedSpeedIndex: Int,
    onSpeedClick: (Int, Pair<String, Float>) -> Unit,
    onDismissRequest: () -> Unit
) {
    val speeds = remember { Speeds.reversedArray() }

    SideSheet(onDismissRequest = onDismissRequest, widthRatio = 0.2f) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                speeds.forEachIndexed { index, speed ->
                    AdaptiveTextButton(
                        text = speed.first,
                        modifier = Modifier.size(MediumTextButtonSize),
                        onClick = { onSpeedClick(index, speed) },
                        color = if (selectedSpeedIndex == index) MaterialTheme.colorScheme.primary else Color.LightGray,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResizeSideSheet(
    selectedResizeIndex: Int,
    onResizeClick: (Int, Pair<String, ResizeMode>) -> Unit,
    onDismissRequest: () -> Unit
) {

    SideSheet(onDismissRequest = onDismissRequest, widthRatio = 0.2f) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Resizes.forEachIndexed { index, resize ->
                AdaptiveTextButton(
                    text = resize.first,
                    modifier = Modifier.size(MediumTextButtonSize),
                    onClick = { onResizeClick(index, resize) },
                    color = if (selectedResizeIndex == index) MaterialTheme.colorScheme.primary else Color.LightGray,
                )
            }
        }
    }
}

@Composable
private fun EpisodeSideSheet(
    episodes: List<Episode>,
    selectedEpisodeIndex: Int,
    onEpisodeClick: (Int, Episode) -> Unit,
    onDismissRequest: () -> Unit
) {
    SideSheet(onDismissRequest = onDismissRequest) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.small_padding)),
            state = rememberLazyGridState(initialFirstVisibleItemIndex = selectedEpisodeIndex, -200)
        ) {
            itemsIndexed(episodes) { index, episode ->
                OutlinedButton(
                    onClick = { onEpisodeClick(index, episode) },
                    contentPadding = PaddingValues(8.dp),
                ) {
                    Text(
                        text = episode.name,
                        color = if (index == selectedEpisodeIndex) MaterialTheme.colorScheme.primary else Color.LightGray,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SideSheet(
    onDismissRequest: () -> Unit,
    widthRatio: Float = 0.4f,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val fullWidth = constraints.maxWidth
        val sideSheetWidthDp = maxWidth * widthRatio

        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(100)
            isVisible = true
        }

        val scope = rememberCoroutineScope()
        val dismissRequestHandle: () -> Unit = {
            isVisible = false
            scope.launch { delay(300) }.invokeOnCompletion { onDismissRequest() }
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { position ->
                    if (position.x < fullWidth - sideSheetWidthDp.toPx()) {
                        dismissRequestHandle()
                    }
                })
            }) {
            AnimatedVisibility(
                visible = isVisible,
                modifier = Modifier.align(Alignment.CenterEnd),
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it }
            ) {
                Column(
                    modifier = Modifier
                        .width(sideSheetWidthDp)
                        .fillMaxHeight()
                        .background(color = Color.Black.copy(alpha = 0.8f))
                        .padding(8.dp)
                ) {
                    content()
                }
            }
        }

        BackHandler {
            dismissRequestHandle()
        }
    }
}

private val MediumTextButtonSize = 42.dp

@Preview(device = Devices.TV_720p)
@Composable
fun SideSheetPreview() {
    AnimeTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            var isSideSheetVisible by remember { mutableStateOf(false) }

            Button(onClick = { isSideSheetVisible = !isSideSheetVisible }) {
                Text(text = "Open")
            }

            if (isSideSheetVisible) {
                SideSheet(onDismissRequest = { isSideSheetVisible = false }) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.small_padding)),
                    ) {
                        items(150) { num ->
                            val isSelected = num % 2 == 0
                            OutlinedButton(
                                onClick = { },
                                contentPadding = PaddingValues(8.dp),
                                border = if (isSelected) BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary
                                ) else ButtonDefaults.outlinedButtonBorder
                            ) {
                                Text(
                                    text = "第2${num}集",
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

