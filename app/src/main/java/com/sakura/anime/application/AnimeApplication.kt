package com.sakura.anime.application

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AnimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        _instance = this

    }

    companion object {
        private lateinit var _instance: Application

        fun getInstance(): Context {
            return _instance
        }
    }
}