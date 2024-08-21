package com.sakura.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.componentsui.anime.domain.model.Episode
import com.sakura.anime.util.EPISODE_TABLE

/**
 * [onDelete = ForeignKey.CASCADE] 当父表中的某一条记录被删除时，子表中所有引用该记录的行也会自动被删除。
 * [onUpdate = ForeignKey.CASCADE] 当父表中的某一条记录的主键被更新时，子表中所有引用该主键的外键也会自动更新为新的值。
 */
@Entity(
    tableName = EPISODE_TABLE,
    indices = [Index("history_id", unique = false), Index("episode_url", unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = HistoryEntity::class,
            parentColumns = ["history_id"],
            childColumns = ["history_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "episode_id") val episodeId: Long = 0L,
    @ColumnInfo(name = "history_id") val historyId: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "episode_url") val episodeUrl: String,
    @ColumnInfo(name = "last_position") val lastPosition: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) {
    fun toEpisode(): Episode {
        return Episode(
            name = name,
            url = episodeUrl,
            lastPosition = lastPosition,
            isPlayed = false,
            historyId = historyId
        )
    }
}
