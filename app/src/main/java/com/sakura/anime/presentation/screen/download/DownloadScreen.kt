package com.sakura.anime.presentation.screen.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sakura.anime.R
import com.sakura.anime.domain.model.Download
import com.sakura.anime.presentation.component.LoadingIndicator
import com.sakura.anime.presentation.component.StateHandler
import com.sakura.anime.util.CROSSFADE_DURATION
import com.sakura.anime.util.LOW_CONTENT_ALPHA
import com.sakura.anime.util.VIDEO_ASPECT_RATIO
import com.sakura.download.utils.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    onBackClick: () -> Unit
) {
    val viewModel: DownloadViewModel = hiltViewModel()
    val downloadState = viewModel.downloadList.collectAsState()

    StateHandler(
        state = downloadState.value,
        onLoading = { LoadingIndicator() }, onFailure = {}) { resource ->

        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(
                        text = stringResource(id = R.string.download_list),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }, navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                })
            }
        ) { paddingValues ->
            resource.data?.let { downloads ->
                LazyColumn(modifier = Modifier.padding(paddingValues)) {
                    items(downloads, key = { item -> item.detailUrl }) { download ->
                        DownloadItem(download = download, onClick = { })
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadItem(
    modifier: Modifier = Modifier,
    download: Download,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
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
                    .data(download.imgUrl)
                    .crossfade(CROSSFADE_DURATION)
                    .build(),
                contentDescription = stringResource(id = R.string.lbl_anime_img),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(dimensionResource(id = R.dimen.image_cover_height))
                    .aspectRatio(VIDEO_ASPECT_RATIO)
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.small_padding)))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = download.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                )

                val sizeStr =
                    stringResource(id = R.string.space_usage) + ": " + download.totalSize.formatSize()

                Text(
                    text = sizeStr,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = LOW_CONTENT_ALPHA),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }

        }
    }

}