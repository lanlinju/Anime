package com.sakura.anime.domain.model

import com.sakura.anime.data.local.entity.FavouriteEntity

data class Favourite(
    val title: String,
    val detailUrl: String,
    val imgUrl: String,
) {
    fun toFavouriteEntity(): FavouriteEntity {
        return FavouriteEntity(
            title = title,
            detailUrl = detailUrl,
            imgUrl = imgUrl
        )
    }
}
