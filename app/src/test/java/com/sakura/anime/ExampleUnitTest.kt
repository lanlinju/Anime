package com.sakura.anime

import com.sakura.anime.data.remote.api.AnimeApiImpl
import com.sakura.anime.data.remote.parse.AnimeJsoupParser
import com.sakura.anime.data.remote.parse.YhdmJsoupParser
import com.sakura.anime.data.repository.AnimeRepositoryImpl
import com.sakura.anime.util.BASE_URL
import com.sakura.anime.util.DownloadManager
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    val api = AnimeApiImpl(
        animeJsoupParser = YhdmJsoupParser,
        downloadManager = DownloadManager
    )
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test_network() {
        runBlocking {
            val html = DownloadManager.getHtml(BASE_URL)
            println(html)
        }
    }

    @Test
    fun test_detail() {
        runBlocking {
            val api = AnimeApiImpl(
                animeJsoupParser = YhdmJsoupParser,
                downloadManager = DownloadManager
            )
            println(api.getAnimeDetail("5042.html"))
        }
    }

    @Test
    fun test_get_video_url() {
        runBlocking {
            val videoHtmlUrl = "/2-1085.html"
            println(api.getVideoUrl(videoHtmlUrl))
        }
    }

    @Test
    fun test_search() {
        runBlocking {
            val query = "海贼王"
            println(api.getSearchData(query))
        }
    }

    @Test
    fun test_week() {
        runBlocking {
            println(api.getWeekDate())
        }
    }

    @Test
    fun  test_dayOfWeek(){
        // 获取当前日期
        val currentDate = LocalDate.now()

        // 获取今天是星期几的数字表示形式（1 表示星期一，2 表示星期二，以此类推）
        val dayOfWeekNumber: Int = currentDate.dayOfWeek.value

        // 输出星期几的数字表示形式
        println("今天是星期：$dayOfWeekNumber")

    }
}