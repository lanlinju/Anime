package com.sakura.anime.data.remote.dto

import com.example.componentsui.anime.domain.model.Home
import com.sakura.anime.domain.model.HomeItem

data class HomeBean(
    val title: String,
    val moreUrl: String,
    val data: List<AnimeBean>
) {
    fun toHome(): Home {
        val homeItems = data.map { anime ->
            HomeItem(
                animTitle = anime.title,
                img = anime.img,
                detailUrl = anime.url,
                episode = anime.episode
            )
        }
        return Home(title = title, animList = homeItems)
    }
}