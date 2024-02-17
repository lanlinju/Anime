package com.sakura.anime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sakura.anime.data.local.entity.DownloadEntity
import com.sakura.anime.data.local.relation.DownloadWithDownloadDetails
import com.sakura.anime.util.DOWNLOAD_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Transaction
    @Query("SELECT * FROM $DOWNLOAD_TABLE ORDER BY created_at DESC")
    fun getDownloads(): Flow<List<DownloadWithDownloadDetails>>

    @Transaction
    @Query("SELECT * FROM $DOWNLOAD_TABLE WHERE detail_url=:detailUrl")
    fun getDownloadWithDownloadDetails(detailUrl: String): Flow<DownloadWithDownloadDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(downloadEntity: DownloadEntity): Long

    @Query("DELETE FROM $DOWNLOAD_TABLE WHERE detail_url=:detailUrl")
    suspend fun deleteDownload(detailUrl: String)

    @Update
    suspend fun updateDownload(downloadEntity: DownloadEntity)

    @Query("SELECT * FROM $DOWNLOAD_TABLE WHERE detail_url=:detailUrl")
    fun getDownload(detailUrl: String): Flow<DownloadEntity>

    @Query("SELECT * FROM $DOWNLOAD_TABLE WHERE detail_url=:detailUrl")
    fun checkDownload(detailUrl: String): Flow<DownloadEntity?>
}