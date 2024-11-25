package com.lanlinju.animius.data.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lanlinju.animius.data.remote.api.AnimeApi
import com.lanlinju.animius.domain.model.Anime
import com.lanlinju.animius.util.SourceMode

class SearchPagingSource(
    private val api: AnimeApi,
    private val query: String,
    private val mode: SourceMode,
) : PagingSource<Int, Anime>() {
    override fun getRefreshKey(state: PagingState<Int, Anime>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Anime> {
        val currentPage = params.key ?: 1

        return try {
            val response = api.getSearchData(query, currentPage, mode)

            val endOfPaginationReached = response.isEmpty()

            LoadResult.Page(
                data = response.map { it.toAnime() },
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (endOfPaginationReached) null else currentPage + 1
            )
        } catch (exp: Exception) {
            LoadResult.Error(exp)
        }

    }
}