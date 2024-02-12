package com.sakura.download.core

import com.sakura.download.Progress
import com.sakura.download.State
import com.sakura.download.helper.Default
import com.sakura.download.utils.clear
import com.sakura.download.utils.closeQuietly
import com.sakura.download.utils.fileName
import com.sakura.download.utils.log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
open class DownloadTask(
    val coroutineScope: CoroutineScope,
    val param: DownloadParam,
    val config: DownloadConfig
) {
    init {
        if (param.savePath.isEmpty()) {
            throw IllegalStateException("the savePath is empty")
        }
    }

    private val stateHolder by lazy { StateHolder() }

    private var downloadJob: Job? = null
    private var downloader: Downloader? = null

    private val downloadStateFlow = MutableStateFlow<State>(stateHolder.none)

    fun isStarted(): Boolean {
        return stateHolder.isStarted()
    }

    fun isFailed(): Boolean {
        return stateHolder.isFailed()
    }

    fun isSucceed(): Boolean {
        return stateHolder.isSucceed()
    }

    fun canStart(): Boolean {
        return stateHolder.canStart()
    }

    private fun checkJob() = downloadJob?.isActive == true

    /**
     * 获取下载文件
     */
    fun file(): File? {
        return if (param.saveName.isNotEmpty()) {
            File(param.savePath, param.saveName)
        } else {
            null
        }
    }

    /**
     * 开始下载，添加到下载队列
     */
    fun start() {
        coroutineScope.launch {
            if (checkJob()) return@launch

            notifyWaiting()
            try {
                config.queue.enqueue(this@DownloadTask)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    notifyFailed()
                }
                e.log()
            }
        }
    }

    /**
     * 开始下载并等待下载完成，直接开始下载，不添加到下载队列
     */
    suspend fun suspendStart() {
        if (checkJob()) return

        downloadJob?.cancel()
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            throwable.log()
            if (throwable !is CancellationException) {
                coroutineScope.launch {
                    notifyFailed()
                }
            }
        }
        downloadJob = coroutineScope.launch(errorHandler + Dispatchers.IO) {
            val response = config.request(param.url, Default.RANGE_CHECK_HEADER)
            try {
                if (!response.isSuccessful || response.body() == null) {
                    throw RuntimeException("request failed")
                }

                if (param.saveName.isEmpty()) {
                    param.saveName = response.fileName()
                }

                if (downloader == null) {
                    downloader = config.dispatcher.dispatch(this@DownloadTask, response)
                }

                notifyStarted()

                val deferred =
                    async(Dispatchers.IO) { downloader?.download(param, config, response) }
                deferred.await()

                notifySucceed()
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    notifyFailed()
                }
                e.log()
            } finally {
                response.closeQuietly()
            }
        }
        downloadJob?.join()
    }

    /**
     * 停止下载
     */
    fun stop() {
        coroutineScope.launch {
            if (isStarted()) {
                config.queue.dequeue(this@DownloadTask)
                downloadJob?.cancel()
                notifyStopped()
            }
        }
    }

    /**
     * 移除任务
     */
    fun remove(deleteFile: Boolean = true) {
        stop()
        config.taskManager.remove(this)
        if (deleteFile) {
            file()?.clear()
        }
    }

    /**
     * @param interval 更新进度间隔时间，单位ms
     */
    fun progress(interval: Long = 200): Flow<Progress> {
        return downloadStateFlow.flatMapLatest {
            channelFlow {
                while (currentCoroutineContext().isActive) {
                    val progress = getProgress()

                    send(progress)

                    if (stateHolder.isEnd() || progress.isComplete()) break

                    delay(interval)
                }
            }
        }
    }

    /**
     * @param interval 更新进度间隔时间，单位ms
     */
    fun state(): Flow<State> {
        return downloadStateFlow
    }

    suspend fun getProgress(): Progress {
        return downloader?.queryProgress() ?: Progress()
    }

    fun getState() = stateHolder.currentState

    private suspend fun notifyWaiting() {
        stateHolder.updateState(stateHolder.waiting)
        downloadStateFlow.value = stateHolder.currentState
    }

    private suspend fun notifyStarted() {
        stateHolder.updateState(stateHolder.downloading)
        downloadStateFlow.value = stateHolder.currentState
    }

    private suspend fun notifyStopped() {
        stateHolder.updateState(stateHolder.stopped)
        downloadStateFlow.value = stateHolder.currentState
    }

    private suspend fun notifyFailed() {
        stateHolder.updateState(stateHolder.failed)
        downloadStateFlow.value = stateHolder.currentState
    }

    private suspend fun notifySucceed() {
        stateHolder.updateState(stateHolder.succeed)
        downloadStateFlow.value = stateHolder.currentState
    }

    private fun Progress.isComplete(): Boolean {
        return totalSize > 0 && totalSize == downloadSize
    }

    class StateHolder {
        val none by lazy { State.None() }
        val waiting by lazy { State.Waiting() }
        val downloading by lazy { State.Downloading() }
        val stopped by lazy { State.Stopped() }
        val failed by lazy { State.Failed() }
        val succeed by lazy { State.Succeed() }

        var currentState: State = none

        fun isStarted(): Boolean {
            return currentState is State.Waiting || currentState is State.Downloading
        }

        fun isFailed(): Boolean {
            return currentState is State.Failed
        }

        fun isSucceed(): Boolean {
            return currentState is State.Succeed
        }

        fun canStart(): Boolean {
            return currentState is State.None || currentState is State.Failed || currentState is State.Stopped
        }

        fun isEnd(): Boolean {
            return currentState is State.None || currentState is State.Waiting || currentState is State.Stopped || currentState is State.Failed || currentState is State.Succeed
        }

        fun updateState(new: State): State {
            currentState = new
            return currentState
        }
    }
}