package com.sakura.anime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sakura.anime.data.local.entity.HistoryEntity
import com.sakura.anime.data.local.relation.HistoryWithEpisodes
import com.sakura.anime.util.HISTORY_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Transaction
    @Query("SELECT * FROM $HISTORY_TABLE ORDER BY updated_at DESC")
    fun getHistories(): Flow<List<HistoryWithEpisodes>>

    @Transaction
    @Query("SELECT * FROM $HISTORY_TABLE WHERE detail_url =:detailUrl")
    fun getHistories(detailUrl: String): Flow<HistoryWithEpisodes>

    @Query("DELETE FROM $HISTORY_TABLE")
    suspend fun deleteAll()

    @Query("DELETE FROM $HISTORY_TABLE WHERE detail_url=:detailUrl")
    suspend fun deleteHistory(detailUrl: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(history: List<HistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Update
    suspend fun updateHistory(history: HistoryEntity)

    @Query("UPDATE $HISTORY_TABLE SET updated_at=:date WHERE detail_url=:detailUrl")
    suspend fun updateHistoryDate(detailUrl: String, date: Long = System.currentTimeMillis())

    @Query("SELECT * FROM $HISTORY_TABLE WHERE detail_url=:detailUrl")
    fun getHistory(detailUrl: String): Flow<HistoryEntity>

    @Query("SELECT * FROM $HISTORY_TABLE WHERE detail_url=:detailUrl")
    fun checkHistory(detailUrl: String): Flow<HistoryEntity?>
}