package com.sakura.anime.presentation.screen.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sakura.anime.R
import com.sakura.anime.presentation.component.LoadingIndicator
import com.sakura.anime.presentation.component.MediaSmall
import com.sakura.anime.presentation.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
) {

    val viewModel = hiltViewModel<SearchViewModel>()
    val animeList by viewModel.animes.collectAsState()

    val isLoading by viewModel.isLoading
        .collectAsState(initial = false)

    val searchQuery by viewModel.query
        .collectAsState()

    SearchBar(
        query = searchQuery,
        onQueryChange = viewModel::onQuery,
        onSearch = viewModel::onSearch,
        active = true,
        onActiveChange = {},
        placeholder = {
            Text(stringResource(id = R.string.lbl_search_placeholder))
        },
        trailingIcon = {
            IconButton(onClick = viewModel::clearSearchQuery) {
                Icon(imageVector = Icons.Rounded.Clear, contentDescription = "")
            }
        }
    ) {
        if (isLoading) {
            LoadingIndicator()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(6.dp)
            ) {
                items(animeList) { anime ->
                    MediaSmall(image = anime.img, label = anime.title, onClick = {
                        navController.navigate(route = Screen.AnimeDetailScreen.passUrl(anime.detailUrl))
                    })
                }
            }

        }
    }

    BackHandler {
        navController.popBackStack()
    }

}

