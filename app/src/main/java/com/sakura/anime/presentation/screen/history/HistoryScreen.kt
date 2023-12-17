package com.sakura.anime.presentation.screen.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sakura.anime.R
import com.sakura.anime.domain.model.History
import com.sakura.anime.presentation.component.LoadingIndicator
import com.sakura.anime.presentation.component.StateHandler
import com.sakura.anime.util.VIDEO_ASPECT_RATIO
import com.sakura.anime.util.CROSSFADE_DURATION
import com.sakura.anime.util.LOW_CONTENT_ALPHA

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onNavigateToAnimeDetail: (detailUrl: String) -> Unit,
    onPlayClick: (episodeUrl: String, title: String) -> Unit
) {
    val viewModel: HistoryViewModel = hiltViewModel()
    val historyListState by viewModel.historyList.collectAsState()
    StateHandler(state = historyListState,
        onLoading = { LoadingIndicator() },
        onFailure = {}
    ) { resource ->
        resource.data?.let { histories ->
            Column(
                modifier = Modifier
                    .systemBarsPadding()
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.play_history)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = stringResource(id = R.string.back)
                            )
                        }
                    })

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.small_padding))
                ) {
                    items(histories, key = { item -> item.detailUrl }) { history ->
                        var expanded by remember { mutableStateOf(false) }
                        val haptic = LocalHapticFeedback.current

                        Box(modifier = Modifier.combinedClickable(
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                expanded = true
                            },
                            onClick = { onNavigateToAnimeDetail(history.detailUrl) }
                        )) {
                            HistoryItem(
                                history = history,
                                onPlayClick = onPlayClick
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                offset = DpOffset(
                                    x = 80.dp * VIDEO_ASPECT_RATIO + 4.dp,
                                    y = (-40).dp
                                ),
                            ) {
                                DropdownMenuItem(text = {
                                    Text(
                                        text = stringResource(id = R.string.delete),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }, onClick = {
                                    expanded = false
                                    viewModel.deleteHistory(history.detailUrl)
                                })
                            }
                        }

                    }
                }
            }
        }
    }

}

@Composable
fun HistoryItem(
    modifier: Modifier = Modifier,
    history: History,
    onPlayClick: (episodeUrl: String, title: String) -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.history_item_height)),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimensionResource(id = R.dimen.small_padding),
                vertical = 4.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.small_padding))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(history.imgUrl)
                    .crossfade(CROSSFADE_DURATION)
                    .build(),
                contentDescription = stringResource(id = R.string.lbl_anime_img),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(VIDEO_ASPECT_RATIO)
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.history_cover_radius)))
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = history.title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                )

                Column {
                    Text(
                        text = history.lastEpisodeName,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = LOW_CONTENT_ALPHA),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    val interactionSource = remember { MutableInteractionSource() }
                    Text(
                        text = stringResource(id = R.string.resume_play),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current
                        ) {
                            onPlayClick(
                                history.lastEpisodeUrl,
                                "${history.title}-${history.lastEpisodeName}"
                            )
                        }
                    )
                }
            }

        }
    }

}

@Preview
@Composable
fun HistoryItemPreview() {
    val histories = MutableList<History>(16) {
        History(
            title = "16bit的感动 ANOTHER LAYER:$it",
            imgUrl = "http://css.yhdmtu.xyz/news/2023/09/27/20230927085556487.jpg",
            lastEpisodeName = "第六集:$it",
            lastEpisodeUrl = "",
            detailUrl = "",
            episodes = emptyList()
        )
    }
    LazyColumn(
        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.small_padding))
    ) {
        items(histories) { history ->
            HistoryItem(history = history, onPlayClick = { _, _ -> })
        }
    }

}