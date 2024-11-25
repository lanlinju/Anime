package com.lanlinju.animius.data.remote.dto

import com.lanlinju.animius.domain.model.Home

data class HomeBean(
    val title: String,
    val moreUrl: String = "",        // 可为空
    val animes: List<AnimeBean>
) {
    fun toHome(): Home {
        val homeItems = animes.map { it.toAnime() }
        return Home(title = title, animeList = homeItems)
    }
}