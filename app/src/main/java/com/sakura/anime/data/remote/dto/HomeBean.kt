package com.sakura.anime.data.remote.dto

import com.example.componentsui.anime.domain.model.Home
import com.sakura.anime.domain.model.HomeItem

data class HomeBean(
    val title: String,
    val moreUrl: String,
    val animes: List<AnimeBean>
) {
    fun toHome(): Home {
        val homeItems = animes.map { anime ->
            HomeItem(
                animTitle = anime.title,
                img = anime.img,
                detailUrl = anime.url,
                episode = anime.episodeName
            )
        }
        return Home(title = title, animList = homeItems)
    }
}