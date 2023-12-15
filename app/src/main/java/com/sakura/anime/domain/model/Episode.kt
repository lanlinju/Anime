package com.example.componentsui.anime.domain.model

data class Episode(
    val name: String,
    val url: String,
    val lastPosition:Long = 0L,
    val isPlayed: Boolean = false
)
