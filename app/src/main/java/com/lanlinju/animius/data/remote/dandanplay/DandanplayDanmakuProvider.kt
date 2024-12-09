package com.lanlinju.animius.data.remote.dandanplay

import com.anime.danmaku.api.DanmakuSession
import com.anime.danmaku.api.TimeBasedDanmakuSession
import com.lanlinju.animius.data.remote.dandanplay.DandanplayDanmakuProvider.Companion.ID
import com.lanlinju.animius.data.remote.dandanplay.dto.toDanmakuOrNull
import com.lanlinju.animius.util.createHttpClient
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A [DanmakuProvider] provides a stream of danmaku for a specific episode.
 *
 * @see DanmakuProviderFactory
 */
interface DanmakuProvider : AutoCloseable {
    // 弹幕提供者的唯一标识符
    val id: String

    // 挂起函数，用于获取弹幕会话
    suspend fun fetch(subjectName: String, episodeName: String?): DanmakuSession?
}

interface DanmakuProviderFactory { // SPI 接口
    /**
     * @see DanmakuProvider.id
     * 获取弹幕提供者的唯一标识符
     */
    val id: String

    // 创建一个新的弹幕提供者实例
    fun create(): DanmakuProvider
}

@Singleton
class DandanplayDanmakuProvider @Inject constructor(
    private val client: HttpClient
) : DanmakuProvider {

    companion object {
        const val ID = "弹弹play"
    }

    override val id: String get() = ID

    private val dandanplayClient = DandanplayClient(client)
    private val moviePattern = Regex("全集|HD|正片")
    private val nonDigitRegex = Regex("\\D")

    override suspend fun fetch(
        subjectName: String, episodeName: String?
    ): DanmakuSession? {
        if (episodeName.isNullOrBlank()) return null
        val formattedEpisodeName = episodeName.let { name ->
            when {
                moviePattern.containsMatchIn(name) -> "movie" // 剧场版
                name.contains("第") -> name.replace(nonDigitRegex, "") // tv 第01集 -> 01
                name.matches(Regex("\\d+")) -> name // girigiri tv的剧集只有数字
                else -> return null // 只获取TV版和剧场版弹幕
            }
        }

        val searchEpisodeResponse =
            dandanplayClient.searchEpisode(subjectName, formattedEpisodeName)

        if (!searchEpisodeResponse.success || searchEpisodeResponse.animes.isEmpty()) {
            return null
        }
        val firstAnime = searchEpisodeResponse.animes[0]
        val episodes = firstAnime.episodes
        if (episodes.isEmpty()) {
            return null
        }
        val firstEpisode = episodes[0]
        val episodeId = firstEpisode.episodeId.toLong()

        return createSession(episodeId)
    }

    private suspend fun createSession(
        episodeId: Long,
    ): DanmakuSession {
        val list = dandanplayClient.getDanmakuList(episodeId = episodeId)
        return TimeBasedDanmakuSession.create(
            list.asSequence().mapNotNull { it.toDanmakuOrNull() },
            coroutineContext = Dispatchers.Default,
        )
    }

    override fun close() {
        //client.close()
    }
}

class DandanplayDanmakuProviderFactory : DanmakuProviderFactory {
    override val id: String get() = ID

    override fun create(): DandanplayDanmakuProvider {
        return DandanplayDanmakuProvider(createHttpClient())
    }
}


