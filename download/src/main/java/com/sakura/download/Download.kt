package com.sakura.download

import com.sakura.download.core.DownloadConfig
import com.sakura.download.core.DownloadParam
import com.sakura.download.core.DownloadTask
import kotlinx.coroutines.CoroutineScope

fun CoroutineScope.download(
    url: String,
    saveName: String = "",
    savePath: String ,
    downloadConfig: DownloadConfig = DownloadConfig()
): DownloadTask {
    val downloadParam = DownloadParam(url, saveName, savePath)
    val task = DownloadTask(this, downloadParam, downloadConfig)
    return downloadConfig.taskManager.add(task)
}

fun CoroutineScope.download(
    downloadParam: DownloadParam,
    downloadConfig: DownloadConfig = DownloadConfig()
): DownloadTask {
    val task = DownloadTask(this, downloadParam, downloadConfig)
    return downloadConfig.taskManager.add(task)
}