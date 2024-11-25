package com.lanlinju.animius.domain.model

import com.lanlinju.animius.data.local.entity.FavouriteEntity
import com.lanlinju.animius.util.SourceMode

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
