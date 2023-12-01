package com.sakura.anime

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
}