package com.lanlinju.animius.domain.repository

import com.anime.danmaku.api.DanmakuSession

interface DanmakuRepository {
    suspend fun fetchDanmakuSession(subjectName: String, episodeName: String?): DanmakuSession?
}