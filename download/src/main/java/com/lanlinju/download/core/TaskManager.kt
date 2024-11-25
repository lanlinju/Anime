package com.lanlinju.download.core

import java.util.concurrent.ConcurrentHashMap

interface TaskManager {
    fun add(task: DownloadTask): DownloadTask

    fun remove(task: DownloadTask)
}

object DefaultTaskManager : TaskManager {
    private val taskMap = ConcurrentHashMap<String, DownloadTask>()

    override fun add(task: DownloadTask): DownloadTask {
        if (taskMap[task.param.tag()] == null) {
            taskMap[task.param.tag()] = task
        }
        return taskMap[task.param.tag()]!!
    }

    override fun remove(task: DownloadTask) {
        taskMap.remove(task.param.tag())
    }
}