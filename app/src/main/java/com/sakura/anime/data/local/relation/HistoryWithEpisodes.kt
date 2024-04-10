package com.sakura.anime.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sakura.anime.data.local.entity.EpisodeEntity
import com.sakura.anime.data.local.entity.HistoryEntity
import com.sakura.anime.domain.model.History
import com.sakura.anime.util.SourceMode
import java.text.SimpleDateFormat
import java.util.Locale

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
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        return History(
            title = history.title,
            imgUrl = history.imgUrl,
            detailUrl = history.detailUrl,
            lastEpisodeName = if (sortedEpisodes.isEmpty()) "" else sortedEpisodes.first().name,
            lastEpisodeUrl = if (sortedEpisodes.isEmpty()) "" else sortedEpisodes.first().episodeUrl,
            sourceMode = SourceMode.valueOf(history.source),
            time = simpleDateFormat.format(history.updatedAt),
            episodes = emptyList()
        )
    }
}
