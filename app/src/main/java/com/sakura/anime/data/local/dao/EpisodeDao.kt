package com.sakura.anime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sakura.anime.data.local.entity.EpisodeEntity
import com.sakura.anime.util.EPISODE_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {

    @Query("SELECT * FROM $EPISODE_TABLE WHERE history_id=:historyId ORDER BY created_at DESC")
    fun getEpisodes(historyId: Long): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM $EPISODE_TABLE WHERE episode_url=:episodeUrl")
    fun getEpisode(episodeUrl: String): Flow<EpisodeEntity?>

    @Query("SELECT * FROM $EPISODE_TABLE WHERE episode_url=:episodeUrl")
    fun checkEpisode(episodeUrl: String): Flow<EpisodeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)

    @Query("DELETE FROM $EPISODE_TABLE")
    suspend fun deleteAll()

    @Update
    suspend fun updateEpisodes(episodes: List<EpisodeEntity>)
}