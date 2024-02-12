package com.sakura.download.core

import com.sakura.download.utils.contentLength
import com.sakura.download.utils.isSupportRange
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.util.concurrent.TimeUnit

interface HttpClientFactory {
    fun create(): OkHttpClient
}

object DefaultHttpClientFactory : HttpClientFactory {
    override fun create(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }
}

interface DownloadDispatcher {
    fun dispatch(downloadTask: DownloadTask, resp: Response<ResponseBody>): Downloader
}

object DefaultDownloadDispatcher : DownloadDispatcher {
    override fun dispatch(downloadTask: DownloadTask, resp: Response<ResponseBody>): Downloader {
        return if (downloadTask.param.url.contains("m3u8")) {
            M3u8Downloader(downloadTask.coroutineScope)
        } else if (downloadTask.config.disableRangeDownload || !resp.isSupportRange()) {
            NormalDownloader(downloadTask.coroutineScope)
        } else {
            RangeDownloader(downloadTask.coroutineScope)
        }
    }
}

interface FileValidator {
    fun validate(
        file: File,
        param: DownloadParam,
        resp: Response<ResponseBody>
    ): Boolean
}

object DefaultFileValidator : FileValidator {
    override fun validate(
        file: File,
        param: DownloadParam,
        resp: Response<ResponseBody>
    ): Boolean {
        return file.length() == resp.contentLength()
    }
}