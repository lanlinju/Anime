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

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

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
            val api = AnimeApiImpl(
                animeJsoupParser = YhdmJsoupParser,
                downloadManager = DownloadManager
            )
            val videoHtmlUrl = "/2-1085.html"
            println(api.getVideoUrl(videoHtmlUrl))
        }
    }
}