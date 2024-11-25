package com.lanlinju.animius.data.remote.api

import com.lanlinju.animius.data.remote.dto.AnimeBean
import com.lanlinju.animius.data.remote.dto.AnimeDetailBean
import com.lanlinju.animius.data.remote.dto.HomeBean
import com.lanlinju.animius.data.remote.dto.VideoBean
import com.lanlinju.animius.util.SourceMode

interface AnimeApi {
    suspend fun getHomeAllData(): List<HomeBean>

    suspend fun getAnimeDetail(detailUrl: String, mode: SourceMode): AnimeDetailBean

    suspend fun getVideoData(episodeUrl: String, mode: SourceMode): VideoBean

    suspend fun getSearchData(query: String, page: Int, mode: SourceMode): List<AnimeBean>

    suspend fun getWeekDate(): Map<Int, List<AnimeBean>>
}