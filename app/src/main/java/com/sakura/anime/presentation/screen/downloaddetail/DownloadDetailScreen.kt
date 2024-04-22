package com.sakura.anime.presentation.screen.downloaddetail

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sakura.anime.R
import com.sakura.anime.presentation.component.BackTopAppBar
import com.sakura.anime.presentation.component.LoadingIndicator
import com.sakura.anime.presentation.component.PopupMenuListItem
import com.sakura.anime.presentation.component.StateHandler
import com.sakura.anime.util.CROSSFADE_DURATION
import com.sakura.anime.util.KEY_FROM_LOCAL_VIDEO
import com.sakura.anime.util.LOW_CONTENT_ALPHA
import com.sakura.anime.util.SourceMode
import com.sakura.anime.util.VIDEO_ASPECT_RATIO
import com.sakura.download.Progress
import com.sakura.download.core.DownloadTask
import com.sakura.download.download
import com.sakura.download.utils.formatSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import com.sakura.download.State as DownloadSate

@Composable
fun DownloadDetailScreen(
    onNavigateToVideoPlay: (episodeUrl: String, mode: SourceMode) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: DownloadDetailViewModel = hiltViewModel()
    val downloadDetailsState = viewModel.downloadDetailsState.collectAsState()
    val titleState = viewModel.title.collectAsState()

    StateHandler(
        state = downloadDetailsState.value,
        onLoading = { LoadingIndicator() },
        onFailure = { }
    ) { resource ->

        Scaffold(topBar = {
            BackTopAppBar(title = titleState.value, onBackClick = onBackClick)
        }) { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                resource.data?.let { downloadDetailList ->
                    items(downloadDetailList, key = { it.downloadUrl }) { downloadDetail ->

                        val state =
                            rememberDownloaderState(
                                url = downloadDetail.downloadUrl,
                                path = downloadDetail.path,
                                isSucceed = downloadDetail.fileSize != 0L,
                                progress = Progress(
                                    downloadSize = downloadDetail.downloadSize,
                                    totalSize = downloadDetail.totalSize
                                ),
                                onSucceed = {
                                    viewModel.updateDownloadDetail(downloadDetail, it)
                                }
                            )

                        SideEffect {
                            // 刷新已经下载完成的
                            if (downloadDetail.fileSize == 0L && state.file.exists()) {
                                state.start()
                            }
                        }

                        PopupMenuListItem(
                            content = {
                                DownloadEpisodeItem(
                                    title = downloadDetail.title,
                                    imgUrl = downloadDetail.imgUrl,
                                    state = state
                                )
                            },
                            menuText = stringResource(id = R.string.delete),
                            onClick = {
                                when {
                                    state.isSucceed.value -> {
                                        val detailUrl = viewModel.detailUrl
                                        val title = titleState.value
                                        val episodeName = downloadDetail.title
                                        val params =
                                            "$KEY_FROM_LOCAL_VIDEO:${detailUrl}:${title}:${episodeName}"

                                        onNavigateToVideoPlay(Uri.encode(params), SourceMode.Yhdm)
                                    }

                                    state.isStarted() -> state.stop()
                                    else -> state.start()
                                }
                            },
                            onMenuItemClick = {
                                viewModel.deleteDownloadDetail(
                                    downloadDetail.downloadUrl,
                                    deleteFile = { state.remove() }
                                )
                            }
                        )

                    }

                }
            }
        }
    }

}

@Composable
fun DownloadEpisodeItem(
    modifier: Modifier = Modifier,
    title: String,
    imgUrl: String,
    state: DownloaderState,
) {
    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(
                    horizontal = dimensionResource(id = R.dimen.small_padding),
                    vertical = dimensionResource(id = R.dimen.small_padding)
                ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.small_padding))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imgUrl)
                    .crossfade(CROSSFADE_DURATION)
                    .build(),
                contentDescription = stringResource(id = R.string.lbl_anime_img),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(dimensionResource(id = R.dimen.image_cover_height))
                    .aspectRatio(VIDEO_ASPECT_RATIO)
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.small_padding)))
                    .blur(if (state.isSucceed.value) 0.dp else 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )

                if (!state.isSucceed.value) {
                    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.small_padding))) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = state.stateMessage.value,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                            )

                            Text(
                                text = state.percentStr.value,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (state.isStarted()) {
                            LinearProgressIndicator(
                                progress = state.progress.value, modifier = Modifier
                                    .height(2.dp),
                                strokeCap = StrokeCap.Round
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.play),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = LOW_CONTENT_ALPHA),
                            style = MaterialTheme.typography.bodySmall,
                        )

                        Text(
                            text = state.file.length().formatSize(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = LOW_CONTENT_ALPHA),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun rememberDownloaderState(
    url: String,
    path: String,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    progress: Progress = Progress(),
    isSucceed: Boolean = false,
    onSucceed: (DownloadTask) -> Unit = { }
): DownloaderState = remember {
    val file = File(path)
    val downloadTask = GlobalScope.download(
        url = url,
        saveName = file.name,
        savePath = file.parent!!
    )
    DownloaderSateImpl(
        downloadTask = downloadTask,
        coroutineScope = coroutineScope,
        context = context,
        onSucceed = onSucceed,
        isSucceed = isSucceed,
        progress = progress,
        file = file
    )
}

class DownloaderSateImpl(
    override val downloadTask: DownloadTask,
    override val file: File,
    private val coroutineScope: CoroutineScope,
    val context: Context,
    progress: Progress,
    isSucceed: Boolean,
    onSucceed: (DownloadTask) -> Unit
) : DownloaderState {
    override val progress = mutableStateOf(progress.progress())
    override val percentStr = mutableStateOf(progress.percentStr())
    override val stateMessage = mutableStateOf("未开始")
    override val isSucceed = mutableStateOf(isSucceed)
    override fun isStarted(): Boolean {
        return downloadTask.isStarted()
    }

    override fun start() {
        downloadTask.start()
    }

    override fun stop() {
        downloadTask.stop()
    }

    override fun remove() {
        downloadTask.remove()
    }

    init {
        coroutineScope.launch {
            launch {
                downloadTask.state().onEach { state ->
                    when (state) {
                        is DownloadSate.None -> stateMessage.value =
                            context.getString(R.string.unstart)

                        is DownloadSate.Waiting -> stateMessage.value =
                            context.getString(R.string.waiting)

                        is DownloadSate.Downloading -> stateMessage.value =
                            context.getString(R.string.downloading)

                        is DownloadSate.Failed -> stateMessage.value =
                            context.getString(R.string.retry)

                        is DownloadSate.Stopped -> stateMessage.value =
                            context.getString(R.string.resume)

                        is DownloadSate.Succeed -> {
                            stateMessage.value = context.getString(R.string.complete)
                            this@DownloaderSateImpl.isSucceed.value = true
                            onSucceed(downloadTask)
                        }
                    }
                }.launchIn(this)
            }

            launch {
                downloadTask.progress().onEach {
                    this@DownloaderSateImpl.progress.value = it.progress()
                    percentStr.value = it.percentStr()
                }.launchIn(this)
            }
        }
    }
}

interface DownloaderState {
    val downloadTask: DownloadTask

    val file: File

    val progress: State<Float>
    val percentStr: State<String>
    val stateMessage: State<String>

    val isSucceed: State<Boolean>

    fun isStarted(): Boolean

    fun start()
    fun stop()
    fun remove()
}