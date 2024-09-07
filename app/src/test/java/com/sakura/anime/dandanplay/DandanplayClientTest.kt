package com.sakura.anime.dandanplay

import com.anime.danmaku.api.DanmakuLocation
import com.sakura.anime.data.remote.dandanplay.DandanplayClient
import com.sakura.anime.data.remote.dandanplay.dto.DandanplayDanmaku
import com.sakura.anime.data.remote.dandanplay.dto.DandanplayDanmakuListResponse
import com.sakura.anime.data.remote.dandanplay.dto.DandanplaySearchEpisodeResponse
import com.sakura.anime.data.remote.dandanplay.dto.SearchAnimeEpisodes
import com.sakura.anime.data.remote.dandanplay.dto.SearchEpisodeDetails
import com.sakura.anime.data.remote.dandanplay.dto.toDanmakuOrNull
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DandanplayClientTest {
    private val testDanmakuResponse = DandanplayDanmakuListResponse(
        count = 2,
        comments = listOf(
            DandanplayDanmaku(
                cid = 12345,
                p = "12.34,1,16777215,98765",
                m = "测试弹幕1"
            ),
            DandanplayDanmaku(
                cid = 67890,
                p = "45.67,4,16711680,54321",
                m = "测试弹幕2"
            )
        )
    )

    @Test
    fun `test getDanmakuList success`() = runBlocking {
        // 使用 MockEngine 模拟 HTTP 请求
        val mockEngine = MockEngine { request ->
            assertEquals(
                "https://api.dandanplay.net/api/v2/comment/123?chConvert=0&withRelated=true",
                request.url.toString()
            )
            respond(
                content = Json.encodeToString(testDanmakuResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        // 实例化 DandanplayClient
        val dandanplayClient = DandanplayClient(client)

        // 调用 getDanmakuList
        val result = dandanplayClient.getDanmakuList(episodeId = 123)

        // 验证结果
        assertEquals(2, result.size)
        val firstDanmaku = result[0]
        assertEquals(12345, firstDanmaku.cid)
        assertEquals("测试弹幕1", firstDanmaku.m)
    }

    @Test
    fun `test toDanmakuOrNull conversion`() {
        val danmaku = DandanplayDanmaku(
            cid = 12345,
            p = "12.34,1,16777215,98765",
            m = "测试弹幕1"
        )

        val result = danmaku.toDanmakuOrNull()

        // 验证转换后的 Danmaku 对象
        assertNotNull(result)
        assertEquals("12345", result?.id)
        assertEquals(12340L, result?.playTimeMillis)
        assertEquals(DanmakuLocation.NORMAL, result?.location)
        assertEquals("测试弹幕1", result?.text)
        assertEquals(16777215, result?.color)
    }

    @Test
    fun `test toDanmakuOrNull invalid data`() {
        val danmaku = DandanplayDanmaku(
            cid = 12345,
            p = "invalid,1,16777215,98765",
            m = "测试弹幕"
        )

        val result = danmaku.toDanmakuOrNull()

        // 验证无效数据返回 null
        assertEquals(null, result)
    }

    // 模拟的响应数据
    private val testSearchResponse = DandanplaySearchEpisodeResponse(
        hasMore = false,
        animes = listOf(
            SearchAnimeEpisodes(
                animeId = 101,
                animeTitle = "Test Anime",
                type = "TV",
                typeDescription = "TV Series",
                episodes = listOf(
                    SearchEpisodeDetails(
                        episodeId = 201,
                        episodeTitle = "Episode 1"
                    ),
                    SearchEpisodeDetails(
                        episodeId = 202,
                        episodeTitle = "Episode 2"
                    )
                )
            )
        ),
        success = true
    )

    @Test
    fun `test searchEpisode success`() = runBlocking {
        // 使用 MockEngine 模拟 HTTP 请求
        val mockEngine = MockEngine { request ->
            assertEquals("Test Anime", request.url.parameters["anime"])
            assertEquals("Episode 1", request.url.parameters["episode"])
            respond(
                content = Json.encodeToString(testSearchResponse),
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        // 实例化 DandanplayClient
        val dandanplayClient = DandanplayClient(client)

        // 调用 searchEpisode 方法
        val result = dandanplayClient.searchEpisode("Test Anime", "Episode 1")

        // 验证结果
        assertTrue(result.success)
        assertEquals(1, result.animes.size)
        assertEquals("Test Anime", result.animes[0].animeTitle)
        assertEquals(2, result.animes[0].episodes.size)
        assertEquals("Episode 1", result.animes[0].episodes[0].episodeTitle)
        assertEquals(201, result.animes[0].episodes[0].episodeId)
    }

    @Test
    fun `test searchEpisode empty episode`() = runBlocking {
        // 使用 MockEngine 模拟 HTTP 请求，不传递 episode 参数
        val mockEngine = MockEngine { request ->
            assertEquals("Test Anime", request.url.parameters["anime"])
            assertEquals(null, request.url.parameters["episode"]) // 没有传递 episode
            respond(
                content = Json.encodeToString(testSearchResponse),
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        // 实例化 DandanplayClient
        val dandanplayClient = DandanplayClient(client)

        // 调用 searchEpisode 方法，不传递 episode 参数
        val result = dandanplayClient.searchEpisode("Test Anime", null)

        // 验证结果
        assertTrue(result.success)
        assertEquals(1, result.animes.size)
        assertEquals("Test Anime", result.animes[0].animeTitle)
    }
}