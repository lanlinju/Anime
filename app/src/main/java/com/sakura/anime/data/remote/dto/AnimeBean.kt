package com.sakura.anime.data.remote.dto

import com.example.componentsui.anime.domain.model.Anime

/**
 * @param title 动漫名称
 * @param img 图片url         /* 获取时间表时可为空 */
 * @param url 动漫详情url
 * @param episodeName 集数
 */
data class AnimeBean(
    val title: String,
    val img: String,
    val url: String,
    val episodeName: String = ""
) {
    fun toAnime(): Anime {
        return Anime(
            title = title,
            img = img,
            detailUrl = url,
            episodeName = episodeName
        )
    }
}