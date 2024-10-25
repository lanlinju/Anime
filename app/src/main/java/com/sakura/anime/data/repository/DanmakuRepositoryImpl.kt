package com.sakura.anime.data.repository

import com.anime.danmaku.api.DanmakuSession
import com.sakura.anime.data.remote.dandanplay.DanmakuProvider
import com.sakura.anime.domain.repository.DanmakuRepository
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