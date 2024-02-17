package com.sakura.anime.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sakura.anime.data.local.entity.DownloadDetailEntity
import com.sakura.anime.data.local.entity.DownloadEntity
import com.sakura.anime.domain.model.Download

data class DownloadWithDownloadDetails(
    @Embedded val download: DownloadEntity,
    @Relation(
        parentColumn = "download_id",
        entityColumn = "download_id",
    )
    val downloadDetails: List<DownloadDetailEntity>
) {
    fun toDownload(): Download {
        val totalSize = downloadDetails.fold(0L) { acc, d -> acc + d.fileSize }
        return Download(
            title = download.title,
            detailUrl = download.detailUrl,
            imgUrl = download.imgUrl,
            source = download.source,
            totalSize = totalSize,
            downloadDetails = emptyList()
        )
    }
}
