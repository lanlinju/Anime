package com.anime.danmaku.api

//import com.anime.danmaku.api.DanmakuCollection

/**
 * A [DanmakuProvider] provides a stream of danmaku for a specific episode.
 *
 * @see DanmakuProviderFactory
 */
interface DanmakuProvider : AutoCloseable {
    val id: String

    /**
     * Matches a danmaku stream by the given filtering parameters.
     *
     * Returns `null` if not found.
     *
     * The returned [DanmakuSession] should be closed when it is no longer needed.
     */
    suspend fun fetch(): DanmakuSession
}

interface DanmakuProviderFactory { // SPI interface
    /**
     * @see DanmakuProvider.id
     */
    val id: String

    fun create(): DanmakuProvider
}

