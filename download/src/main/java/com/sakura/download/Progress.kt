package com.sakura.download

import com.sakura.download.utils.formatSize
import com.sakura.download.utils.ratio

/*
 *如果下载的文件是m3u8类型则downloadSize，totalSize表示的是ts文件数量而不是字节数
 */
class Progress(
    var downloadSize: Long = 0,
    var totalSize: Long = 0,
    /**
     * 用于标识一个链接是否是分块下载, 如果该值为true, 那么totalSize为-1
     */
    var isChunked: Boolean = false
) {
    /**
     * Return total size str. eg: 10M
     */
    fun totalSizeStr(): String {
        return totalSize.formatSize()
    }

    /**
     * Return download size str. eg: 3M
     */
    fun downloadSizeStr(): String {
        return downloadSize.formatSize()
    }

    /**
     * Return percent number.
     */
    fun percent(): Double {
        if (isChunked) return 0.0
        return downloadSize ratio totalSize
    }

    /**
     * Return percent string.
     */
    fun percentStr(): String {
        return "${percent()}%"
    }

    /**
     * Return progress value. Range 0.0 - 1.0
     */
    fun progress(): Float {
        return (percent() * 0.01f).toFloat()
    }
}