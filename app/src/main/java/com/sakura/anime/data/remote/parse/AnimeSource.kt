package com.sakura.anime.data.remote.parse

import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.data.remote.dto.AnimeDetailBean
import com.sakura.anime.data.remote.dto.HomeBean
import com.sakura.anime.data.remote.dto.VideoBean
import com.sakura.anime.util.preferences

interface AnimeSource {

    /**
     * [preferences] 的Key值用于获取用户的自定义的域名
     */
    val KEY_SOURCE_DOMAIN: String
        get() = "${this.javaClass.simpleName}Domain"

    /**
     * 默认动漫域名
     */
    val DEFAULT_DOMAIN: String

    /**
     * 动漫域名，默认值为[DEFAULT_DOMAIN]，
     * 且[DEFAULT_DOMAIN] 要先于 [baseUrl] 初始化
     */
    var baseUrl: String

    suspend fun getHomeData(): List<HomeBean>

    suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean

    suspend fun getVideoData(episodeUrl: String): VideoBean

    suspend fun getSearchData(query: String, page: Int): List<AnimeBean>

    suspend fun getWeekData(): Map<Int, List<AnimeBean>>

    /**
     * 当切换选中的数据源时调用，可以执行一些初始化操作
     */
    fun onEnter() {}

    /**
     * 当退出当前数据源时调用，可以执行一些清理操作
     */
    fun onExit() {}
}