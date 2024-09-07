package com.sakura.anime.data.remote.dandanplay

import com.sakura.anime.data.remote.dandanplay.dto.DandanplayDanmaku
import com.sakura.anime.data.remote.dandanplay.dto.DandanplayDanmakuListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType

class DandanplayClient(
    private val client: HttpClient,
) {
    suspend fun getDanmakuList(
        episodeId: Long,
    ): List<DandanplayDanmaku> {
        val chConvert = 0
        val response =
            client.get("https://api.dandanplay.net/api/v2/comment/${episodeId}?chConvert=$chConvert&withRelated=true") {
                accept(ContentType.Application.Json)
            }.body<DandanplayDanmakuListResponse>()
        return response.comments
    }
}