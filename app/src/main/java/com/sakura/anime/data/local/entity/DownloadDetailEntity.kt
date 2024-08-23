package com.sakura.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sakura.anime.domain.model.DownloadDetail
import com.sakura.anime.util.DOWNLOAD_DETAIL_TABLE

@Entity(
    tableName = DOWNLOAD_DETAIL_TABLE,
    indices = [Index("download_id", unique = false), Index("download_url", unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = DownloadEntity::class,
            parentColumns = ["download_id"],
            childColumns = ["download_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DownloadDetailEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "download_detail_id") val downloadDetailId: Long = 0,
    @ColumnInfo(name = "download_id") val downloadId: Long,
    @ColumnInfo(name = "title") val title: String, /* 剧集名 eg: 第01集 */
    @ColumnInfo(name = "img_url") val imgUrl: String,
    @ColumnInfo(name = "drama_number") val dramaNumber: Int = 0, /* 用于集数排序 */
    @ColumnInfo(name = "download_url") val downloadUrl: String,
    @ColumnInfo(name = "path") val path: String, /* 保存的文件路径 */
    @ColumnInfo(name = "download_size") val downloadSize: Long = 0, /* 同下 */
    @ColumnInfo(name = "total_size") val totalSize: Long = 0, /* 如果是m3u8类型则是分片数量，其他文件表示字节数 */
    @ColumnInfo(name = "file_size") val fileSize: Long = 0, /* 在文件下载成功后写入其大小 */
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) {
    fun toDownloadDetail(): DownloadDetail {
        return DownloadDetail(
            title = title,
            imgUrl = imgUrl,
            dramaNumber = dramaNumber,
            downloadUrl = downloadUrl,
            path = path,
            downloadSize = downloadSize,
            totalSize = totalSize,
            fileSize = fileSize,
        )
    }
}
