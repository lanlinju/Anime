package com.sakura.anime.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sakura.anime.data.local.entity.EpisodeEntity
import com.sakura.anime.data.local.entity.HistoryEntity
import com.sakura.anime.domain.model.History
import com.sakura.anime.util.SourceMode

data class HistoryWithEpisodes(
    @Embedded val history: HistoryEntity,
    @Relation(
        parentColumn = "history_id",
        entityColumn = "history_id"
    )
    val episodes: List<EpisodeEntity>
) {
    fun toHistory(): History {
        val sortedEpisodes = episodes.sortedByDescending { it.createdAt }
        return History(
            title = history.title,
            imgUrl = history.imgUrl,
            detailUrl = history.detailUrl,
            lastEpisodeName = sortedEpisodes.first().name,
            lastEpisodeUrl = sortedEpisodes.first().episodeUrl,
            sourceMode = SourceMode.valueOf(history.source),
            episodes = emptyList()
        )
    }
}
