package com.anime.danmaku.api

//import com.anime.danmaku.api.DanmakuCollection

/**
 * A [DanmakuProvider] provides a stream of danmaku for a specific episode.
 *
 * @see DanmakuProviderFactory
 */
interface DanmakuProvider : AutoCloseable {
    // 弹幕提供者的唯一标识符
    val id: String

    // 挂起函数，用于获取弹幕会话
    suspend fun fetch(): DanmakuSession
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

