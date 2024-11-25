package com.lanlinju.animius.di

import com.lanlinju.animius.data.remote.api.AnimeApi
import com.lanlinju.animius.data.remote.api.AnimeApiImpl
import com.lanlinju.animius.data.remote.dandanplay.DandanplayDanmakuProvider
import com.lanlinju.animius.data.remote.dandanplay.DanmakuProvider
import com.lanlinju.animius.data.repository.AnimeRepositoryImpl
import com.lanlinju.animius.data.repository.DanmakuRepositoryImpl
import com.lanlinju.animius.domain.repository.AnimeRepository
import com.lanlinju.animius.domain.repository.DanmakuRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApiModule {
    @Singleton
    @Binds
    abstract fun providesAnimeApi(animeApiImpl: AnimeApiImpl): AnimeApi

    @Singleton
    @Binds
    abstract fun providesAnimeRepository(animeRepositoryImpl: AnimeRepositoryImpl): AnimeRepository

    @Singleton
    @Binds
    abstract fun provideDandanplayProvider(dandanplayDanmakuProvider: DandanplayDanmakuProvider): DanmakuProvider

    @Singleton
    @Binds
    abstract fun provideDanmakuRepository(danmakuRepositoryImpl: DanmakuRepositoryImpl): DanmakuRepository
}