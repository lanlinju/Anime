package com.lanlinju.animius.data.repository

import com.anime.danmaku.api.DanmakuSession
import com.lanlinju.animius.data.remote.dandanplay.DanmakuProvider
import com.lanlinju.animius.domain.repository.DanmakuRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DanmakuRepositoryImpl @Inject constructor(
    private val danmakuProvider: DanmakuProvider
) : DanmakuRepository {
    override suspend fun fetchDanmakuSession(
        subjectName: String,
        episodeName: String?
    ): DanmakuSession? {
        return try {
            danmakuProvider.fetch(subjectName, episodeName)
        } catch (_: Exception) {
            null
        }
    }
}