package com.lanlinju.animius.presentation.component

import androidx.compose.runtime.Composable
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

@Composable
fun <T : Any> PaginationStateHandler(
    paginationState: LazyPagingItems<T>,
    loadingComponent: @Composable () -> Unit,
    errorComponent: @Composable ((Throwable) -> Unit)? = null
) {

    paginationState.apply {
        when {
            (loadState.refresh is LoadState.Loading)
                    or (loadState.append is LoadState.Loading)
                    or (loadState.prepend is LoadState.Loading) -> loadingComponent()

            (loadState.refresh is LoadState.Error) -> {
                errorComponent?.invoke((loadState.refresh as LoadState.Error).error)
            }
            (loadState.append is LoadState.Error) -> {
                errorComponent?.invoke((loadState.append as LoadState.Error).error)
            }
            (loadState.prepend is LoadState.Error) -> {
                errorComponent?.invoke((loadState.prepend as LoadState.Error).error)
            }
        }
    }

}