package com.sakura.anime.presentation.screen.animedetail

import android.content.res.Configuration
import android.text.Html
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.componentsui.anime.domain.model.Episode
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
    viewModel:AnimeDetailViewModel = hiltViewModel(),
    onRelatedAnimeClick: (detailUrl: String) -> Unit,
    onEpisodeClick: (episodeUrl: String) -> Unit
) {
    val scrollState = rememberScrollState()
    val bannerHeight = dimensionResource(Res.dimen.banner_height)

    val animeDetailState by viewModel.animeDetailState.collectAsState()

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
                        .verticalScroll(scrollState)
                        .navigationBarsPadding()
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
                            .fillMaxHeight()
                            .padding(
                                top = bannerHeight,
                                bottom = dimensionResource(Res.dimen.large_padding)
                            )
                            .background(MaterialTheme.colorScheme.background),
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
                                .fillMaxSize()
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

                        AnimeEpisodes(
                            episodes = animeDetail.episodes,
                            contentPadding = PaddingValues(
                                start = dimensionResource(Res.dimen.large_padding) + if (
                                    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                                ) {
                                    WindowInsets.displayCutout.asPaddingValues()
                                        .calculateLeftPadding(LayoutDirection.Ltr)
                                } else 0.dp,
                                end = dimensionResource(Res.dimen.large_padding)
                            ),
                            onEpisodeClick = onEpisodeClick
                        )

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
                            modifier = Modifier.width(dimensionResource(Res.dimen.media_card_width))
                        )
                    }
                }
            }
        }
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


@OptIn(ExperimentalMaterial3Api::class)
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
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    onEpisodeClick: (episodeUrl: String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(Res.dimen.medium_padding)
        ),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        items(episodes) { episode ->
            FilledTonalButton(
                onClick = { onEpisodeClick(episode.url) },
                colors = ButtonDefaults.buttonColors(containerColor = color.copy(0.5f))
            ) {
                Text(
                    text = episode.name,
                    color = MaterialTheme.colorScheme.onBackground,
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