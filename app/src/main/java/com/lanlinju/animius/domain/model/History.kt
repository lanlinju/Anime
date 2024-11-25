package com.lanlinju.animius.domain.model

import com.lanlinju.animius.data.local.entity.HistoryEntity
import com.lanlinju.animius.util.SourceMode

data class History(
    val title: String,
    val imgUrl: String,
    val detailUrl: String,
    val lastEpisodeName: String = "",
    val lastEpisodeUrl: String = "",
    val sourceMode: SourceMode,
    val time: String = "",
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
