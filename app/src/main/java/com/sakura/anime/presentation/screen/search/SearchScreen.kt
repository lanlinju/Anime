package com.sakura.anime.presentation.screen.search


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.sakura.anime.R
import com.sakura.anime.presentation.component.MediaSmall
import com.sakura.anime.presentation.component.PaginationStateHandler
import com.sakura.anime.presentation.component.WarningMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToAnimeDetail: (detailUrl: String) -> Unit,
    onBackClick: () -> Unit
) {

    val viewModel = hiltViewModel<SearchViewModel>()
    val animesState = viewModel.animesState.collectAsLazyPagingItems()
    val searchQuery by viewModel.query.collectAsState()

    SearchBar(
        query = searchQuery,
        onQueryChange = viewModel::onQuery,
        onSearch = viewModel::onSearch,
        active = true,
        onActiveChange = { if (!it) onBackClick() },
        leadingIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        },
        placeholder = {
            Text(stringResource(id = R.string.lbl_search_placeholder))
        },
        trailingIcon = {
            IconButton(onClick = viewModel::clearSearchQuery) {
                Icon(imageVector = Icons.Rounded.Clear, contentDescription = "")
            }
        },
        modifier = Modifier.navigationBarsPadding()
    ) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(6.dp)
        ) {
            items(count = animesState.itemCount) { index ->
                val item = animesState[index]!!
                MediaSmall(image = item.img, label = item.title, onClick = {
                    onNavigateToAnimeDetail(item.detailUrl)
                })
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                PaginationStateHandler(
                    paginationState = animesState,
                    loadingComponent = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = dimensionResource(
                                        id = R.dimen.medium_padding
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    },
                    errorComponent = {
                        WarningMessage(
                            textId = R.string.txt_empty_result,
                            onRetryClick = {
                                animesState.retry()
                            }
                        )
                    }
                )
            }

        }

    }

}

