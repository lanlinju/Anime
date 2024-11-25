package com.lanlinju.animius.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lanlinju.animius.data.local.entity.FavouriteEntity
import com.lanlinju.animius.util.FAVOURITE_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM $FAVOURITE_TABLE ORDER BY created_at DESC")
    fun getAllFavourites(): Flow<List<FavouriteEntity>>

    @Query("SELECT * FROM $FAVOURITE_TABLE  WHERE detail_url = :detailUrl")
    fun getFavouriteByDetailUrl(detailUrl: String): Flow<FavouriteEntity>

    @Query("SELECT * FROM $FAVOURITE_TABLE  WHERE detail_url = :detailUrl")
    fun checkFavourite(detailUrl: String): Flow<FavouriteEntity?>

    @Delete
    suspend fun deleteFavourite(favourite: FavouriteEntity)

    @Query("DELETE FROM $FAVOURITE_TABLE  WHERE detail_url = :detailUrl")
    suspend fun deleteFavouriteByDetailUrl(detailUrl: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(favourite: FavouriteEntity)
}