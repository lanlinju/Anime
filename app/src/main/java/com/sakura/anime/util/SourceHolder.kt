package com.sakura.anime.util

import com.sakura.anime.data.remote.parse.AgedmSource
import com.sakura.anime.data.remote.parse.AnimeSource
import com.sakura.anime.data.remote.parse.MxdmSource
import com.sakura.anime.data.remote.parse.SilisiliSource
import com.sakura.anime.data.remote.parse.YhdmSource

object SourceHolder {
    private var _currentSource: AnimeSource = YhdmSource
    val currentSource: AnimeSource
        get() = _currentSource

    var isSourceChange = false

    /**
     *当启动应用时调用，设置默认的数据源，切换数据源请用方法[SourceHolder].switchSource()
     */
    fun setDefaultSource(mode: SourceMode) {
        _currentSource = getSource(mode)
        _currentSource.onEnter()
    }

    /**
     *切换数据源
     */
    fun switchSource(mode: SourceMode) {
        _currentSource.onExit()

        _currentSource = getSource(mode)

        _currentSource.onEnter()
    }

    /**
     * 根据[SourceMode]获取对应的[AnimeSource]数据源
     * */
    fun getSource(mode: SourceMode): AnimeSource {
        return when (mode) {
            SourceMode.Yhdm -> YhdmSource
            SourceMode.Silisili -> SilisiliSource
            SourceMode.Mxdm -> MxdmSource
            SourceMode.Agedm -> AgedmSource
        }
    }
}