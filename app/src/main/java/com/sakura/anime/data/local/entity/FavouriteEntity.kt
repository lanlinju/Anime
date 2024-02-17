package com.sakura.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sakura.anime.domain.model.Favourite
import com.sakura.anime.util.FAVOURITE_TABLE

@Entity(
    tableName = FAVOURITE_TABLE,
    indices = [Index("detail_url", unique = true)]
)
data class FavouriteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "favourite_id") val favouriteId: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "detail_url") val detailUrl: String,
    @ColumnInfo(name = "img_url") val imgUrl: String,
    @ColumnInfo(name = "source") val source: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) {
    fun toFavourite(): Favourite {
        return Favourite(
            title = title,
            detailUrl = detailUrl,
            imgUrl = imgUrl
        )
    }
}
