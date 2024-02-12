package com.sakura.download.helper

object Default {
    /**
     * 默认的分片大小
     */
    const val DEFAULT_RANGE_SIZE = 5L * 1024 * 1024

    /**
     * 单个任务同时下载的分片数量
     */
    const val DEFAULT_RANGE_CURRENCY = 5

    /**
     * 同时下载的任务数量
     */
    const val MAX_TASK_NUMBER = 3

    /**
     * 默认的Header
     */
    val RANGE_CHECK_HEADER = mapOf("Range" to "bytes=0-")
}