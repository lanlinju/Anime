package com.sakura.anime.domain.model

import com.example.componentsui.anime.domain.model.Episode
import com.sakura.anime.data.local.entity.HistoryEntity
import com.sakura.anime.util.SourceMode

data class History(
    val title: String,
    val imgUrl: String,
    val detailUrl: String,
    val lastEpisodeName: String = "",
    val lastEpisodeUrl: String = "",
    val sourceMode: SourceMode,
    val episodes: List<Episode>
) {
    fun toHistoryEntity(): HistoryEntity {
        return HistoryEntity(
            title = title,
            imgUrl = imgUrl,
            detailUrl = detailUrl,
            source = sourceMode.name
        )
    }
}
