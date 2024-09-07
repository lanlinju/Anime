package com.sakura.anime.dandanplay

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoviePatternTest {
    private val moviePattern = Regex("全集|HD|正片")

    @Test
    fun testMoviePattern_Matches() {
        // 测试匹配“全集”
        assertTrue(moviePattern.containsMatchIn("全集"))

        // 测试匹配“HD”
        assertTrue(moviePattern.containsMatchIn("高清HD"))

        // 测试匹配“正片”
        assertTrue(moviePattern.containsMatchIn("正片"))
    }

    @Test
    fun testMoviePattern_DoesNotMatch() {
        // 测试不匹配的字符串
        assertFalse(moviePattern.containsMatchIn("第01集"))
        assertFalse(moviePattern.containsMatchIn("预告片"))
        assertFalse(moviePattern.containsMatchIn("其他"))
    }
}