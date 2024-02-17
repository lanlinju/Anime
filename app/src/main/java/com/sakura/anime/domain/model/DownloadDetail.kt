package com.sakura.anime.domain.model

import com.sakura.anime.data.local.entity.DownloadDetailEntity

data class DownloadDetail(
    val title: String, /* 剧集名 eg: 第01集 */
    val imgUrl: String,
    val dramaNumber: Int, /* 用于集数排序 */
    val downloadUrl: String,
    val path: String, /* 保存的文件路径 */
    val downloadSize: Long = 0, /* 同下 */
    val totalSize: Long = 0, /* 如果是m3u8类型则是分片数量，其他文件表示字节数 */
    val fileSize: Long = 0, /* 在文件下载成功后写入其大小 */
) {
    fun toDownloadDetailEntity(downloadId: Long): DownloadDetailEntity {
        return DownloadDetailEntity(
            downloadId = downloadId,
            title = title,
            imgUrl = imgUrl,
            downloadUrl = downloadUrl,
            dramaNumber = dramaNumber,
            path = path,
            downloadSize = downloadSize,
            totalSize = totalSize,
            fileSize = fileSize,
        )
    }
}
