package com.sakura.anime.presentation.screen.animedetail

import android.content.Context
import android.content.res.Configuration
import android.text.Html
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.componentsui.anime.domain.model.Anime
import com.example.componentsui.anime.domain.model.AnimeDetail
import com.example.componentsui.anime.domain.model.Episode
import com.sakura.anime.domain.model.Favourite
import com.sakura.anime.domain.model.History
import com.sakura.anime.presentation.component.LoadingIndicator
import com.sakura.anime.presentation.component.MediaSmall
import com.sakura.anime.presentation.component.MediaSmallRow
import com.sakura.anime.presentation.component.ScrollableText
import com.sakura.anime.presentation.component.StateHandler
import com.sakura.anime.presentation.component.TranslucentStatusBarLayout
import com.sakura.anime.presentation.component.WarningMessage
import com.sakura.anime.util.CROSSFADE_DURATION
import com.sakura.anime.util.bannerParallax
import com.sakura.anime.R as Res

@Composable
fun AnimeDetailScreen(
    viewModel: AnimeDetailViewModel = hiltViewModel(),
    onRelatedAnimeClick: (detailUrl: String) -> Unit,
    onEpisodeClick: (episodeUrl: String, title: String) -> Unit
) {
    val scrollState = rememberScrollState()
    val bannerHeight = dimensionResource(Res.dimen.banner_height)

    val animeDetailState by viewModel.animeDetailState.collectAsState()
    val isFavourite by viewModel.isFavourite.collectAsState()

    val view = LocalContext.current

    StateHandler(
        state = animeDetailState,
        onLoading = { LoadingIndicator() },
        onFailure = { WarningMessage(textId = Res.string.txt_empty_result) }
    ) { resource ->
        resource.data?.let { animeDetail ->
            TranslucentStatusBarLayout(
                scrollState = scrollState,
                distanceUntilAnimated = bannerHeight
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(scrollState)
                ) {
                    AnimeBanner(
                        imageUrl = animeDetail.img,
                        tintColor = Color(0).copy(alpha = 0.25f),
                        modifier = Modifier
                            .height(bannerHeight)
                            .fillMaxWidth()
                            .bannerParallax(scrollState)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = bannerHeight),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(Res.dimen.large_padding))
                    ) {
                        AnimeDetails(
                            title = animeDetail.title,
                            description = Html
                                .fromHtml(animeDetail.desc, Html.FROM_HTML_MODE_COMPACT)
                                .toString(),
                            modifier = Modifier
                                .padding(
                                    start = dimensionResource(Res.dimen.large_padding)
                                            + dimensionResource(Res.dimen.media_card_width)
                                            + dimensionResource(Res.dimen.large_padding),
                                    top = dimensionResource(Res.dimen.medium_padding),
                                    end = dimensionResource(Res.dimen.large_padding)
                                )
                                .height(
                                    WindowInsets.statusBars
                                        .asPaddingValues()
                                        .calculateTopPadding()
                                            + dimensionResource(Res.dimen.media_card_top_padding)
                                            + dimensionResource(Res.dimen.media_card_height)
                                            - dimensionResource(Res.dimen.banner_height)
                                            - dimensionResource(Res.dimen.medium_padding)
                                )
                        )

                        AnimeGenres(
                            genres = animeDetail.tags,
                            contentPadding = PaddingValues(
                                start = dimensionResource(Res.dimen.large_padding) + if (
                                    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                                ) {
                                    WindowInsets.displayCutout.asPaddingValues()
                                        .calculateLeftPadding(LayoutDirection.Ltr)
                                } else 0.dp,
                                end = dimensionResource(Res.dimen.large_padding)
                            ),
                            color = MaterialTheme.colorScheme.inversePrimary
                        )

                        Box {
                            var reverseList by remember {
                                mutableStateOf(false)
                            }
                            AnimeEpisodes(
                                episodes = animeDetail.episodes,
                                lastPosition = animeDetail.lastPosition,
                                contentPadding = PaddingValues(
                                    start = dimensionResource(Res.dimen.large_padding) + if (
                                        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                                    ) {
                                        WindowInsets.displayCutout.asPaddingValues()
                                            .calculateLeftPadding(LayoutDirection.Ltr)
                                    } else 0.dp,
                                    end = dimensionResource(Res.dimen.large_padding)
                                ),
                                reverseList = reverseList,
                                onEpisodeClick = { episode ->
                                    val history =
                                        History(
                                            title = animeDetail.title,
                                            imgUrl = animeDetail.img,
                                            detailUrl = viewModel.detailUrl,
                                            episodes = listOf(episode)
                                        )
                                    viewModel.addHistory(history)
                                    onEpisodeClick(
                                        episode.url,
                                        "${animeDetail.title}-${episode.name}"
                                    )
                                }
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = dimensionResource(id = Res.dimen.large_padding) + 4.dp)
                                    .padding(
                                        top = dimensionResource(id = Res.dimen.small_padding),
                                        end = dimensionResource(id = Res.dimen.small_padding)
                                    )
                                    .align(Alignment.BottomEnd),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.clickable {
                                        reverseList = !reverseList
                                    },
                                    text = stringResource(id = Res.string.reverse_list),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.size(12.dp))
                                Text(
                                    text = stringResource(id = Res.string.more_episodes),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = stringResource(id = Res.string.more_episodes),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        AnimeRelated(
                            animes = animeDetail.relatedAnimes,
                            contentPadding = PaddingValues(horizontal = dimensionResource(Res.dimen.large_padding)),
                            onRelatedAnimeClick = onRelatedAnimeClick
                        )

                    }

                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(
                                top = dimensionResource(Res.dimen.media_card_top_padding),
                                start = dimensionResource(Res.dimen.large_padding),
                                end = dimensionResource(Res.dimen.large_padding)
                            )
                    ) {
                        MediaSmall(
                            image = animeDetail.img,
                            label = null,
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.width(dimensionResource(Res.dimen.media_card_width))
                        )

                        FavouriteIcon(isFavourite, animeDetail, viewModel, view)
                    }
                }
            }
        }
    }
}

@Composable
private fun FavouriteIcon(
    isFavourite: Boolean,
    animeDetail: AnimeDetail,
    viewModel: AnimeDetailViewModel,
    view: Context
) {
    val msg = stringResource(
        id = if (!isFavourite) Res.string.add_favourite else Res.string.remove_favourite
    )
    IconButton(
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.45f
            )
        ),
        onClick = {
            val favourite = Favourite(
                animeDetail.title,
                viewModel.detailUrl,
                animeDetail.img
            )
            Toast.makeText(view, msg, Toast.LENGTH_SHORT).show()
            viewModel.favourite(favourite)
        }) {
        Icon(
            if (isFavourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = stringResource(id = Res.string.favourite),
            tint = Color.White,
        )
    }
}

@Composable
fun AnimeBanner(
    imageUrl: String?,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    if (!imageUrl.isNullOrEmpty()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(CROSSFADE_DURATION)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            alignment = Alignment.Center,
            colorFilter = ColorFilter.tint(
                color = tintColor,
                blendMode = BlendMode.SrcAtop
            )
        )
    } else {
        Image(
            painter = painterResource(Res.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier,
            alignment = Alignment.TopCenter,
            colorFilter = ColorFilter.tint(
                color = tintColor,
                blendMode = BlendMode.SrcAtop
            )
        )
    }
}

@Composable
fun AnimeDetails(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        ScrollableText(text = description)
    }
}


@Composable
fun AnimeGenres(
    genres: List<String>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    color: Color = MaterialTheme.colorScheme.primaryContainer
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(Res.dimen.medium_padding)
        ),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        items(genres) { genre ->
            SuggestionChip(
                label = {
                    Text(
                        text = genre.lowercase(),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(
                            vertical = dimensionResource(Res.dimen.small_padding)
                        )
                    )
                },
                onClick = { },
                shape = CircleShape,
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = color.copy(alpha = 0.25f)
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    borderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun AnimeEpisodes(
    episodes: List<Episode>,
    lastPosition: Int,
    modifier: Modifier = Modifier,
    reverseList: Boolean,
    contentPadding: PaddingValues,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    onEpisodeClick: (episode: Episode) -> Unit
) {
    val scrollState = rememberLazyListState(lastPosition)

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(Res.dimen.medium_padding)
        ),
        contentPadding = contentPadding,
        modifier = modifier,
        state = scrollState
    ) {
        items(if (!reverseList) episodes else episodes.reversed()) { episode ->
            FilledTonalButton(
                onClick = { onEpisodeClick(episode) },
                colors = ButtonDefaults.buttonColors(containerColor = color.copy(0.5f))
            ) {
                Text(
                    text = episode.name,
                    color = if (episode.isPlayed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(
                        vertical = dimensionResource(Res.dimen.small_padding)
                    )
                )
            }
        }
    }
}

@Composable
fun AnimeRelated(
    animes: List<Anime>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onRelatedAnimeClick: (detailUrl: String) -> Unit
) {
    Column(modifier) {
        Text(
            text = stringResource(Res.string.lbl_related_anime),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(contentPadding)
        )

        Spacer(Modifier.size(dimensionResource(Res.dimen.medium_padding)))

        MediaSmallRow(
            mediaList = animes
        ) { anime ->
            MediaSmall(
                image = anime.img,
                label = anime.title,
                onClick = { onRelatedAnimeClick(anime.detailUrl) },
                modifier = Modifier.width(dimensionResource(Res.dimen.media_card_width))
            )
        }
    }
}