package com.sakura.anime.di

import com.sakura.anime.data.remote.api.AnimeApi
import com.sakura.anime.data.remote.api.AnimeApiImpl
import com.sakura.anime.data.repository.AnimeRepositoryImpl
import com.sakura.anime.domain.repository.AnimeRepository
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
}