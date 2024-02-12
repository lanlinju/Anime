package com.sakura.download.core

import com.sakura.download.utils.closeQuietly
import com.sakura.download.utils.contentLength
import com.sakura.download.utils.isChunked
import com.sakura.download.utils.recreate
import com.sakura.download.utils.shadow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import okhttp3.ResponseBody
import okio.buffer
import okio.sink
import retrofit2.Response
import java.io.File

class NormalDownloader(coroutineScope: CoroutineScope) : BaseDownloader(coroutineScope) {
    companion object {
        private const val BUFFER_SIZE = 8192L
    }

    private var alreadyDownloaded = false

    private lateinit var file: File
    private lateinit var shadowFile: File

    override suspend fun download(
        downloadParam: DownloadParam,
        downloadConfig: DownloadConfig,
        response: Response<ResponseBody>
    ) {
        try {
            file = downloadParam.file()
            shadowFile = file.shadow()

            val contentLength = response.contentLength()
            val isChunked = response.isChunked()

            downloadPrepare(downloadParam, contentLength)

            if (alreadyDownloaded) {
                this.downloadSize = contentLength
                this.totalSize = contentLength
                this.isChunked = isChunked
            } else {
                this.totalSize = contentLength
                this.downloadSize = 0
                this.isChunked = isChunked
                startDownload(response.body()!!)
            }
        } finally {
            response.closeQuietly()
        }
    }

    private fun downloadPrepare(downloadParam: DownloadParam, contentLength: Long) {
        //make sure dir is exists
        val fileDir = downloadParam.dir()
        if (!fileDir.exists() || !fileDir.isDirectory) {
            fileDir.mkdirs()
        }

        if (file.exists()) {
            if (file.length() == contentLength) {
                alreadyDownloaded = true
            } else {
                file.delete()
                shadowFile.recreate()
            }
        } else {
            shadowFile.recreate()
        }
    }

    private suspend fun startDownload(body: ResponseBody) = coroutineScope {
        val deferred = async(Dispatchers.IO) {
            val source = body.source()
            val sink = shadowFile.sink().buffer()
            val buffer = sink.buffer

            var readLen = source.read(buffer, BUFFER_SIZE)
            while (isActive && readLen != -1L) {
                downloadSize += readLen
                readLen = source.read(buffer, BUFFER_SIZE)
                sink.flush()
            }
            sink.flush()
        }
        deferred.await()

        if (isActive) {
            shadowFile.renameTo(file)
        }
    }
}