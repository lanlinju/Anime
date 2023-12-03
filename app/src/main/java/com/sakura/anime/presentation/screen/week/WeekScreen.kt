package com.sakura.anime.presentation.screen.week

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura.anime.R
import com.sakura.anime.presentation.component.LoadingIndicator
import com.sakura.anime.presentation.component.StateHandler
import com.sakura.anime.presentation.component.WarningMessage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.util.GITHUB_ADDR
import com.sakura.anime.util.TABS
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(
    onNavigateToAnimeDetail: (detailUrl: String) -> Unit,
    onSearchClick: () -> Unit
) {
    val viewModel = hiltViewModel<WeekViewModel>()
    val weekDataState by viewModel.weeKDataMap.collectAsState()

    val scope = rememberCoroutineScope()
    val dayOfWeek = LocalDate.now().dayOfWeek.value - 1
    val pagerState = rememberPagerState(initialPage = dayOfWeek, pageCount = { TABS.size })

    Column(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = dimensionResource(R.dimen.navigation_bar_height))
            .navigationBarsPadding()
    ) {
        var expanded by remember { mutableStateOf(false) }
        val uriHandler = LocalUriHandler.current
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.lbl_schedule),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(id = R.string.more)
                    )
                }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(id = R.string.search)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("GitHub仓库") },
                            onClick = {
                                expanded = false
                                uriHandler.openUri(GITHUB_ADDR)
                            },
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_github),
                                    contentDescription = null
                                )
                            })
                    }
                }
            })

        TabRow(
            selectedTabIndex = pagerState.currentPage,
        ) {
            TABS.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title, fontSize = 12.sp) },
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.scrollToPage(index) } },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 1,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            StateHandler(
                state = weekDataState,
                onLoading = { LoadingIndicator() },
                onFailure = { WarningMessage(textId = R.string.txt_empty_result) }
            ) { resource ->
                resource.data?.let { weekDataMap ->
                    weekDataMap[TABS[page]]?.let { list ->
                        WeekList(
                            list = list,
                            onItemClicked = {
                                onNavigateToAnimeDetail(it.url)
                            })
                    }
                }
            }
        }
    }

}

@Composable
fun WeekList(
    list: List<AnimeBean>,
    onItemClicked: (AnimeBean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(6.dp)
    ) {
        items(list) { anime ->
            WeekItem(
                title = anime.title,
                subtitle = anime.episode,
                onClick = { onItemClicked(anime) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.height(80.dp),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = title,
                modifier = Modifier.align(Alignment.TopStart),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = subtitle,
                modifier = Modifier.align(Alignment.BottomEnd),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
