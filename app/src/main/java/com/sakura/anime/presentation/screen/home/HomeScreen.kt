package com.sakura.anime.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura.anime.domain.model.HomeItem
import com.sakura.anime.R as Res
import com.sakura.anime.presentation.component.LoadingHome
import com.sakura.anime.presentation.component.MediaSmall
import com.sakura.anime.presentation.component.MediaSmallRow
import com.sakura.anime.presentation.component.StateHandler
import com.sakura.anime.presentation.component.TranslucentStatusBarLayout
import com.sakura.anime.presentation.component.WarningMessage
import com.sakura.anime.util.bannerParallax

@Composable
fun HomeScreen() {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val availableDataList = homeViewModel.homeDataList.collectAsState()

    StateHandler(state = availableDataList.value, onLoading = {
        LoadingHome()
    }, onFailure = {
        WarningMessage(textId = Res.string.txt_empty_result)
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
                Box {
                    Image(
                        painter = painterResource(Res.drawable.background),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(Res.dimen.banner_height))
                            .bannerParallax(scrollState),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.secondaryContainer.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                )
                            )
                            .fillMaxWidth()
                            .height(dimensionResource(Res.dimen.banner_height))
                    )

                    Text(
                        text = stringResource(Res.string.lbl_anime),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(
                                start = dimensionResource(Res.dimen.large_padding),
                                bottom = dimensionResource(Res.dimen.medium_padding)
                            )
                    )
                }

                Column {
                    Spacer(Modifier.size(dimensionResource(Res.dimen.banner_height)))

                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = dimensionResource(Res.dimen.large_padding)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(Res.dimen.large_padding))
                    ) {
                        resource.data?.forEach { home ->
                            HomeRow(
                                list = home.animList,
                                title = home.title,
                                onItemClicked = { }
                            )
                        }
                        
                    }

                }
            }
        }
    }
}


@Composable
fun HomeRow(
    list: List<HomeItem?>,
    title: String,
    onItemClicked: (HomeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(
                    start = dimensionResource(Res.dimen.large_padding)
                )
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