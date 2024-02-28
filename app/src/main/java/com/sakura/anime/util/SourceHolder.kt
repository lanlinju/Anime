package com.sakura.anime.util

import com.sakura.anime.data.remote.parse.AnimeSource
import com.sakura.anime.data.remote.parse.MxdmSource
import com.sakura.anime.data.remote.parse.SilisiliSource
import com.sakura.anime.data.remote.parse.YhdmSource

object SourceHolder {
    private var _currentSource: AnimeSource = YhdmSource
    val currentSource: AnimeSource
        get() = _currentSource

    var isSourceChange = false

    fun updateSource(mode: SourceMode) {
        _currentSource = getSource(mode)
    }

    fun getSource(mode: SourceMode): AnimeSource {
        return when (mode) {
            SourceMode.Yhdm -> YhdmSource
            SourceMode.Silisili -> SilisiliSource
            SourceMode.Mxdm -> MxdmSource
        }
    }
}