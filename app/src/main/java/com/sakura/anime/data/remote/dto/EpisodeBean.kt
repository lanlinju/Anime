package com.sakura.anime.data.remote.dto

import com.example.componentsui.anime.domain.model.Episode

data class EpisodeBean(val name: String, val url: String){
    fun  toEpisode(): Episode {
        return Episode(name = name, url = url)
    }
}