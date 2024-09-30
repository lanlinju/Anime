package com.sakura.anime.presentation.screen.favourite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura.anime.R
import com.sakura.anime.presentation.component.LoadingIndicator
import com.sakura.anime.presentation.component.MediaSmall
import com.sakura.anime.presentation.component.SourceBadge
import com.sakura.anime.presentation.component.StateHandler
import com.sakura.anime.util.SourceMode
import com.sakura.anime.util.isWideScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(
    onNavigateToAnimeDetail: (detailUrl: String, mode: SourceMode) -> Unit,
) {
    val favouriteViewModel: FavouriteViewModel = hiltViewModel()
    val availableDataList = favouriteViewModel.favouriteList.collectAsState()

    StateHandler(state = availableDataList.value, onLoading = {
        LoadingIndicator()
    }, onFailure = {}) { resource ->
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.my_favourite),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                )
            },
            contentWindowInsets = WindowInsets.systemBars.exclude(WindowInsets.navigationBars)
        ) { paddingValues ->

            val context = LocalContext.current

            LazyVerticalGrid(
                modifier = Modifier
                    .padding(paddingValues),
                columns = if (isWideScreen(context)) GridCells.Adaptive(dimensionResource(R.dimen.min_media_card_width)) else GridCells.Fixed(
                    3
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                resource.data?.let { favouriteList ->
                    items(favouriteList) { anime ->

                        var expanded by remember { mutableStateOf(false) }
                        val haptic = LocalHapticFeedback.current

                        Box {
                            SourceBadge(
                                text = anime.sourceMode.name,
                                isAlignmentStart = false,
                                style = MaterialTheme.typography.bodySmall
                            ) {
                                MediaSmall(
                                    image = anime.imgUrl,
                                    label = anime.title,
                                    onClick = {
                                        onNavigateToAnimeDetail(
                                            anime.detailUrl,
                                            anime.sourceMode
                                        )
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        expanded = true
                                    }
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                offset = DpOffset(
                                    x = 40.dp,
                                    y = (-100).dp
                                ),
                            ) {

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(id = R.string.delete),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        expanded = false
                                        favouriteViewModel.removeFavourite(anime.detailUrl)
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