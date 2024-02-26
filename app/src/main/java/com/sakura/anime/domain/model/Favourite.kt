package com.sakura.anime.domain.model

import com.sakura.anime.data.local.entity.FavouriteEntity
import com.sakura.anime.util.SourceMode

data class Favourite(
    val title: String,
    val detailUrl: String,
    val imgUrl: String,
    val sourceMode: SourceMode
) {
    fun toFavouriteEntity(): FavouriteEntity {
        return FavouriteEntity(
            title = title,
            detailUrl = detailUrl,
            imgUrl = imgUrl,
            source = sourceMode.name
        )
    }
}
