package com.lanlinju.download

import com.lanlinju.download.core.DownloadConfig
import com.lanlinju.download.core.DownloadParam
import com.lanlinju.download.core.DownloadTask
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