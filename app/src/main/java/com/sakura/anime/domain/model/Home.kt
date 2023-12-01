package com.example.componentsui.anime.domain.model

import com.sakura.anime.domain.model.HomeItem

data class Home(
    val title:String,
    val animList: List<HomeItem>
)