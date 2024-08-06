package com.sakura.download.core

import com.sakura.download.Progress
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

class QueryProgress(val completableDeferred: CompletableDeferred<Progress>)

interface Downloader {
    var actor: SendChannel<QueryProgress>

    suspend fun queryProgress(): Progress

    /**
     * 用于计算下载网速
     * [M3u8Downloader] 需要重写这个方法
     */
    suspend fun queryDownloadSize(): Long = queryProgress().downloadSize

    suspend fun download(
        downloadParam: DownloadParam,
        downloadConfig: DownloadConfig,
        response: Response<ResponseBody>
    )
}

@OptIn(ObsoleteCoroutinesApi::class, DelicateCoroutinesApi::class)
abstract class BaseDownloader(protected val coroutineScope: CoroutineScope) : Downloader {
    protected var totalSize: Long = 0L
    protected var downloadSize: Long = 0L
    protected var isChunked: Boolean = false

    private val progress = Progress()

    override var actor = GlobalScope.actor<QueryProgress>(Dispatchers.IO) {
        for (each in channel) {
            each.completableDeferred.complete(progress.also {
                it.downloadSize = downloadSize
                it.totalSize = totalSize
                it.isChunked = isChunked
            })
        }
    }

    override suspend fun queryProgress(): Progress {
        val ack = CompletableDeferred<Progress>()
        val queryProgress = QueryProgress(ack)
        actor.send(queryProgress)
        return ack.await()
    }

    fun DownloadParam.dir(): File {
        return File(savePath)
    }

    fun DownloadParam.file(): File {
        return File(savePath, saveName)
    }
}