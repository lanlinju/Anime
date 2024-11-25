package com.lanlinju.download.core

import com.lanlinju.download.helper.Default.MAX_TASK_NUMBER
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import java.util.concurrent.*

interface DownloadQueue {
    suspend fun enqueue(task: DownloadTask)

    suspend fun dequeue(task: DownloadTask)
}

@OptIn(DelicateCoroutinesApi::class)
class DefaultDownloadQueue private constructor(private val maxTask: Int) : DownloadQueue {
    companion object {
        private val lock = Any()
        private var instance: DefaultDownloadQueue? = null

        fun get(maxTask: Int = MAX_TASK_NUMBER): DefaultDownloadQueue {
            if (instance == null) {
                synchronized(lock) {
                    if (instance == null) {
                        instance = DefaultDownloadQueue(maxTask)
                    }
                }
            }
            return instance!!
        }
    }

    private val channel = Channel<DownloadTask>()
    private val tempMap = ConcurrentHashMap<String, DownloadTask>()

    init {
        GlobalScope.launch {
            repeat(maxTask) {
                launch {
                    channel.consumeEach {
                        if (contain(it)) {
                            it.suspendStart()
                            dequeue(it)
                        }
                    }
                }
            }
        }
    }

    override suspend fun enqueue(task: DownloadTask) {
        tempMap[task.param.tag()] = task
        channel.send(task)
    }

    override suspend fun dequeue(task: DownloadTask) {
        tempMap.remove(task.param.tag())
    }

    private fun contain(task: DownloadTask): Boolean {
        return tempMap[task.param.tag()] != null
    }
}