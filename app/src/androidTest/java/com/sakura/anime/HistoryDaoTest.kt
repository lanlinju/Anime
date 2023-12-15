package com.sakura.anime

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sakura.anime.data.local.dao.HistoryDao
import com.sakura.anime.data.local.database.AnimeDatabase
import com.sakura.anime.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class HistoryDaoTest {
    private lateinit var historyDao: HistoryDao
    private lateinit var animeDatabase: AnimeDatabase

    private var history1 = HistoryEntity(1, "海贼王1", "img1", "/video1")
    private var history2 = HistoryEntity(2, "海贼王2", "img2", "/video2")

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        animeDatabase = Room.inMemoryDatabaseBuilder(context, AnimeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        historyDao = animeDatabase.historyDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        animeDatabase.close()
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsHistoryIntoDB() = runBlocking {
        addTwoHistoriesToDb()
        val allHistories = historyDao.getHistories().first()
        Assert.assertEquals(allHistories[0].history, history1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetHistory_returnsHistoryFromDB() = runBlocking {
        addOneHistoryToDb()
        val favourite = historyDao.getHistories("/video1")
        Assert.assertEquals(favourite.first().history, history1)
    }

    @Test
    @Throws(Exception::class)
    fun daoCheckHistory_returnsNullFromDB() = runBlocking {
        val favourite = historyDao.checkHistory("/video3").first()
        Assert.assertNull(favourite)
    }

    @Test
    @Throws(Exception::class)
    fun daoCheckHistory_returnsNotNullFromDB() = runBlocking {
        addOneHistoryToDb()
        val favourite = historyDao.checkHistory("/video1").first()
        Assert.assertNotNull(favourite)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_returnsHistoryIdFromDB() = runBlocking {
        val historyId1 = historyDao.insertHistory(history1)
        val historyId2 = historyDao.getHistory(history1.detailUrl).first()
        Assert.assertEquals(historyId1, historyId2.historyId)
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdate_updateHistoryDate() = runBlocking {
        addOneHistoryToDb()
        historyDao.updateHistoryDate(history1.detailUrl)
        val actualHistory = historyDao.getHistory(history1.detailUrl).first()
        Assert.assertNotEquals(history1.updatedAt, actualHistory.updatedAt)
    }

    private suspend fun addOneHistoryToDb() {
        historyDao.insertHistory(history1)
    }

    private suspend fun addTwoHistoriesToDb() {
        historyDao.insertHistory(history1)
        historyDao.insertHistory(history2)
    }
}