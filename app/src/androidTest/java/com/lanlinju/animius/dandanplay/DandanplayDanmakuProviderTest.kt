package com.lanlinju.animius.dandanplay

import com.lanlinju.animius.data.remote.dandanplay.DandanplayDanmakuProviderFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test

class DandanplayDanmakuProviderTest {
    private val provider = DandanplayDanmakuProviderFactory().create()

    @Test
    fun testFetchTVDanmakuSession() = runBlocking {

        val session = provider.fetch("海贼王", "第11话")

        assertNotNull(session)
    }

    @Test
    fun testFetchMovieDanmakuSession() = runBlocking {

        val session = provider.fetch("紫罗兰永恒花园 剧场版", "全集")

        assertNotNull(session)
    }

}