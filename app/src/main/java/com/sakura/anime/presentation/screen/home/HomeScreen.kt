package com.sakura.anime.presentation.screen.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sakura.anime.domain.model.HomeItem
import com.sakura.anime.presentation.component.LoadingIndicator
import com.sakura.anime.presentation.component.MediaSmall
import com.sakura.anime.presentation.component.MediaSmallRow
import com.sakura.anime.presentation.component.StateHandler
import com.sakura.anime.presentation.component.TranslucentStatusBarLayout
import com.sakura.anime.presentation.component.WarningMessage
import com.sakura.anime.util.KEY_HOME_BACKGROUND_URI
import com.sakura.anime.util.SourceHolder
import com.sakura.anime.util.SourceMode
import com.sakura.anime.util.bannerParallax
import com.sakura.anime.util.isWideScreen
import com.sakura.anime.util.rememberPreference
import com.sakura.anime.R as Res

@Composable
fun HomeScreen(
    onNavigateToAnimeDetail: (detailUrl: String, mode: SourceMode) -> Unit,
) {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val availableDataList = homeViewModel.homeDataList.collectAsState()

    LaunchedEffect(SourceHolder.isSourceChanged) {
        if (SourceHolder.isSourceChanged) {
            homeViewModel.refresh()
            SourceHolder.isSourceChanged = false
        }
    }

    Column(
        Modifier.fillMaxSize()
    ) {
        StateHandler(state = availableDataList.value, onLoading = {
            LoadingIndicator()
        }, onFailure = {
            WarningMessage(
                textId = Res.string.txt_empty_result,
                onRetryClick = { homeViewModel.refresh() }
            )
        }) { resource ->

            val scrollState = rememberScrollState()

            TranslucentStatusBarLayout(
                scrollState = scrollState,
                distanceUntilAnimated = dimensionResource(Res.dimen.banner_height)
            ) {
                Box(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                ) {

                    val context = LocalContext.current
                    val isWideScreen = isWideScreen(context)

                    if (!isWideScreen) {
                        HomeBackground(scrollState = scrollState)
                    }

                    Column {
                        Spacer(Modifier.size(if (!isWideScreen) dimensionResource(Res.dimen.banner_height) else 0.dp))

                        Column(
                            modifier = Modifier
                                .background(
                                    if (!isWideScreen)
                                        Color(
                                            ColorUtils.blendARGB(
                                                MaterialTheme.colorScheme.background.toArgb(),
                                                MaterialTheme.colorScheme.primaryContainer.toArgb(),
                                                0.05f
                                            )
                                        ) else MaterialTheme.colorScheme.background
                                )
                                .padding(vertical = dimensionResource(Res.dimen.large_padding)),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(Res.dimen.large_padding)),
                        ) {
                            if (isWideScreen) {
                                HomeTile()
                            }

                            resource.data?.forEach { home ->
                                HomeRow(
                                    list = home.animList,
                                    title = home.title,
                                    onItemClicked = {
                                        onNavigateToAnimeDetail(
                                            it.detailUrl,
                                            SourceHolder.currentSourceMode
                                        )
                                    }
                                )
                            }

                        }

                    }
                }
            }
        }
    }
}

@Composable
fun HomeBackground(scrollState: ScrollState) {
    Box {
        val context = LocalContext.current
        var imageUri by rememberPreference(key = KEY_HOME_BACKGROUND_URI, defaultValue = "")

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) {
            it?.let { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = uri.toString()
            }
        }

        AsyncImage(
            model = Uri.parse(imageUri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(Res.dimen.banner_height))
                .bannerParallax(scrollState),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            error = painterResource(Res.drawable.background)
        )

        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    )
                )
                .fillMaxWidth()
                .height(dimensionResource(Res.dimen.banner_height))
        )

        HomeTile(modifier = Modifier.align(Alignment.BottomStart)) {
            launcher.launch(arrayOf("image/*"))
        }

    }
}

@Composable
private fun HomeTile(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isWideScreen = isWideScreen(LocalContext.current)

    Box(
        modifier = modifier
            .padding(
                start = dimensionResource(Res.dimen.large_padding),
                bottom = if (!isWideScreen) dimensionResource(Res.dimen.medium_padding) else 0.dp
            )
    ) {
        if (!isWideScreen) {
            Text(
                text = stringResource(Res.string.lbl_anime),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .clickable { onClick() }
            )
        }

        Text(
            text = SourceHolder.currentSourceMode.name,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = 8.dp)
                .run {
                    if (isWideScreen) {
                        // 获取焦点
                        clickable { }
                    } else this
                }
        )
    }
}

@Composable
fun HomeRow(
    list: List<HomeItem?>,
    title: String,
    onItemClicked: (HomeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(start = dimensionResource(Res.dimen.large_padding))
        )

        Spacer(Modifier.size(dimensionResource(Res.dimen.medium_padding)))

        MediaSmallRow(
            mediaList = list,
            content = { homeItem ->
                MediaSmall(
                    image = homeItem?.img,
                    // TODO: Do something about this chain.
                    label = homeItem?.animTitle,
                    onClick = {
                        onItemClicked(homeItem!!)
                    },
                    modifier = Modifier.width(dimensionResource(Res.dimen.media_card_width))
                )
            }
        )
    }
}