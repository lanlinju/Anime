package com.sakura.anime.presentation.screen.favourite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura.anime.R
import com.sakura.anime.presentation.component.LoadingIndicator
import com.sakura.anime.presentation.component.MediaSmall
import com.sakura.anime.presentation.component.StateHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(
    onNavigateToAnimeDetail: (detailUrl: String) -> Unit,
    onBackClick: () -> Unit
) {
    val favouriteViewModel: FavouriteViewModel = hiltViewModel()
    val availableDataList = favouriteViewModel.favouriteList.collectAsState()

    StateHandler(state = availableDataList.value, onLoading = {
        LoadingIndicator()
    }, onFailure = {}) { resource ->
        Scaffold(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
            topBar = {
                TopAppBar(title = {
                    Text(
                        text = stringResource(id = R.string.my_favourite),
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
            }) { paddingValues ->

            LazyVerticalGrid(
                modifier = Modifier.padding(paddingValues),
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(6.dp)
            ) {
                resource.data?.let { favouriteList ->
                    items(favouriteList) { anime ->
                        MediaSmall(image = anime.imgUrl, label = anime.title, onClick = {
                            onNavigateToAnimeDetail(anime.detailUrl)
                        })
                    }
                }
            }
        }

    }
}