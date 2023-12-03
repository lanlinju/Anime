package com.sakura.anime.presentation.screen.videoplay

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.sakura.video_player.rememberVideoPlayerState

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
            activity.requestedOrientation =
                if (playerState.isFullscreen.value) {
                    hideSystemBars(LocalView.current)
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    showSystemBars(LocalView.current)
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

            Box(contentAlignment = Alignment.Center) {
                val localView = LocalView.current
                localView.keepScreenOn = true
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
                        title = "",
                        onBackClick = {
                            playerState.control.setFullscreen(false)
                            onBackClick()
                        }
                    )
                }

                if (playerState.isLoading.value && !playerState.isError.value) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                if (playerState.isError.value) {
                    ShowVideoMessage(stringResource(id = R.string.video_error_msg))
                }

                if (playerState.isEnded.value) {
                    ShowVideoMessage(stringResource(id = R.string.video_ended_msg))
                }
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