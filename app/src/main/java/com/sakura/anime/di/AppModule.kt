package com.sakura.anime.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.sakura.anime.application.AnimeApplication
import com.sakura.anime.data.local.database.AnimeDatabase
import com.sakura.anime.data.remote.parse.AnimeJsoupParser
import com.sakura.anime.data.remote.parse.YhdmJsoupParser
import com.sakura.anime.data.repository.RoomRepositoryImpl
import com.sakura.anime.domain.repository.RoomRepository
import com.sakura.anime.util.ANIME_DATABASE
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
    fun providesAnimeApplication(
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

    @Singleton
    @Provides  // The Application binding is available without qualifiers.
    fun providesDatabase(application: Application): AnimeDatabase {
        return Room.databaseBuilder(
            application,
            AnimeDatabase::class.java,
            ANIME_DATABASE,
        ).build()
    }

    @Singleton
    @Provides
    fun providesRoomRepository(database: AnimeDatabase): RoomRepository {
        return RoomRepositoryImpl(database)
    }
}