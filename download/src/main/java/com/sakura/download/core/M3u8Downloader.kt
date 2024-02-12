package com.sakura.download.core

import com.sakura.download.helper.Default.RANGE_CHECK_HEADER
import com.sakura.download.utils.decrypt
import com.sakura.download.utils.handleFormat
import com.sakura.download.utils.mappedByteBuffer
import com.sakura.download.utils.parallel
import com.sakura.download.utils.recreate
import com.sakura.download.utils.shadow
import com.sakura.download.utils.tmp
import com.sakura.download.utils.tsFile
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.BufferedOutputStream
import java.io.File

@OptIn(ObsoleteCoroutinesApi::class)
class M3u8Downloader(coroutineScope: CoroutineScope) : BaseDownloader(coroutineScope) {

    private lateinit var file: File
    private lateinit var shadowFile: File
    private lateinit var tmpFile: File
    private lateinit var rangeTmpFile: RangeTmpFile

    private lateinit var m3u8Parse: M3u8Parser
    private lateinit var tsUrlList: List<String>
    private lateinit var key: String
    private lateinit var iv: String

    override suspend fun download(
        downloadParam: DownloadParam,
        downloadConfig: DownloadConfig,
        response: Response<ResponseBody>
    ) {
        file = downloadParam.file()
        shadowFile = file.shadow()
        tmpFile = file.tmp()

        m3u8Parse = M3u8Parser(downloadParam)
        m3u8Parse.parseM3u8(downloadParam.url, downloadConfig)
        tsUrlList = m3u8Parse.getTsUrlList()
        key = m3u8Parse.getKey()
        iv = m3u8Parse.getIv()

        totalSize = tsUrlList.size.toLong()

        val alreadyDownloaded = checkFiles(downloadParam)

        if (alreadyDownloaded) {
            downloadSize = totalSize
        } else {
            val last = rangeTmpFile.lastProgress(true)
            downloadSize = last.downloadSize
            startDownload(downloadConfig)
        }
    }

    private fun checkFiles(param: DownloadParam): Boolean {
        var alreadyDownloaded = false

        //确保目录存在
        val fileDir = param.dir()
        if (!fileDir.exists() || !fileDir.isDirectory) {
            fileDir.mkdirs()
        }

        if (file.exists()) {
            alreadyDownloaded = true
        } else {
            if (tmpFile.exists()) {
                rangeTmpFile = RangeTmpFile(tmpFile)
                rangeTmpFile.read()
            } else {
                tmpFile.recreate()
                shadowFile.recreate()
                rangeTmpFile = RangeTmpFile(tmpFile)
                rangeTmpFile.write(totalSize, tsUrlList.size.toLong(), -1L)
            }
        }

        return alreadyDownloaded
    }

    /**
     * 开始下载
     */
    private suspend fun startDownload(config: DownloadConfig) {
        val progressChannel = coroutineScope.actor<Int> {
            channel.consumeEach {
                downloadSize += it
            }
        }

        rangeTmpFile.undoneRanges().parallel(max = config.rangeCurrency, dispatcher = Dispatchers.IO) {
            it.download(config, progressChannel)
        }

        progressChannel.close()

        mergeTsFile()

        shadowFile.renameTo(file)
        tmpFile.delete()
    }

    private suspend fun Range.download(
        config: DownloadConfig,
        sendChannel: SendChannel<Int>,
    ) = coroutineScope {
        val deferred = async(Dispatchers.IO) {
            val url = tsUrlList.get(index.toInt())
            val rangeHeader = if (end == 0L) RANGE_CHECK_HEADER else mapOf("Range" to "bytes=${current}-${end}")
            val tsFile = file.tsFile(index)

            val response = config.request(url, rangeHeader)
            if (!response.isSuccessful || response.body() == null) {
                throw RuntimeException("Request failed!")
            }

            response.body()?.use {
                it.byteStream().use { source ->
                    val tsRemainSize = if (end == 0L) it.contentLength() else remainSize()
                    val tsFileBuffer = tsFile.mappedByteBuffer(current, tsRemainSize)
                    val tmpFileBuffer = tmpFile.mappedByteBuffer(startByte(), Range.RANGE_SIZE)
                    if (end == 0L) tmpFileBuffer.putLong(24, it.contentLength() - 1)

                    val buffer = ByteArray(8192)
                    var readLen = source.read(buffer)

                    while (isActive && readLen != -1) {
                        tsFileBuffer.put(buffer, 0, readLen)
                        current += readLen

                        tmpFileBuffer.putLong(16, current)

                        readLen = source.read(buffer)
                    }
                    //send发送下载完成的ts文件数量
                    sendChannel.send(1)
                }
            }
        }
        deferred.await()
    }

    // 对已完成的Ts文件进行验证
    private fun validateTs() {
        rangeTmpFile.read()
        val completedRanges = rangeTmpFile.completedRanges()
        for (range in completedRanges) {
            val tsFile = file.tsFile(range.index)

            if (tsFile.length() != range.completeSize()) {
                throw IllegalStateException("failed to download ts file: ${tsFile.name}")
            }
        }
    }

    private suspend fun mergeTsFile() {
        withContext(Dispatchers.Default) {

            validateTs()

            BufferedOutputStream(shadowFile.outputStream()).use { output ->
                file.tsFile(0).handleFormat()

                for (index in 0 until tsUrlList.size) {
                    val tsFile = file.tsFile(index.toLong())

                    val bytes = if (key.isEmpty()) {
                        tsFile.readBytes()
                    } else {
                        tsFile.readBytes().decrypt(key, iv)
                    }

                    output.write(bytes)

                    tsFile.delete()
                }
            }
        }
    }

}

