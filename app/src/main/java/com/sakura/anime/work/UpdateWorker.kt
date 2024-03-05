package com.sakura.anime.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sakura.anime.util.KEY_DOWNLOAD_UPDATE_URL
import com.sakura.anime.util.installApk
import com.sakura.download.download
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class UpdateWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_DOWNLOAD_UPDATE_URL) ?: return Result.failure()

        return withContext(Dispatchers.IO) {
            try {
                val savePath = applicationContext.getExternalFilesDir("apk/")!!.path
                val downloadTask = download(url = url, saveName = "base.apk", savePath = savePath)
                downloadTask.suspendStart()

                if (downloadTask.isSucceed()) {
                    val file = File(savePath, "base.apk")
                    applicationContext.installApk(file)
                    Result.success()
                } else {
                    Result.failure()
                }
            } catch (e: Exception) {
                Result.failure()
            }
        }

    }
}