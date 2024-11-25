package com.lanlinju.animius.domain.model

import com.lanlinju.animius.data.local.entity.DownloadEntity
import com.lanlinju.animius.util.SourceMode

data class Download(
    val title: String,
    val detailUrl: String,
    val imgUrl: String,
    val sourceMode: SourceMode,
    val totalSize: Long = 0, /* 已下载完成的全部剧集大小，单位字节 */
    val downloadDetails: List<DownloadDetail>
) {
    fun toDownloadEntity(): DownloadEntity {
        return DownloadEntity(
            title = title,
            detailUrl = detailUrl,
            imgUrl = imgUrl,
            source = sourceMode.name,
        )
    }

}
