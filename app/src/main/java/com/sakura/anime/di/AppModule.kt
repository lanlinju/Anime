package com.sakura.anime.di

import android.content.Context
import com.sakura.anime.application.AnimeApplication
import com.sakura.anime.data.remote.parse.AnimeJsoupParser
import com.sakura.anime.data.remote.parse.YhdmJsoupParser
import com.sakura.anime.util.DownloadManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    fun provideAnimeApplication(
        @ApplicationContext app: Context
    ): AnimeApplication {
        return app as AnimeApplication
    }

    @Singleton
    @Provides
    fun providesAnimeJsoupParser(): AnimeJsoupParser {
        return YhdmJsoupParser
    }

    @Singleton
    @Provides
    fun providesDownloadManager(): DownloadManager {
        return DownloadManager
    }
}