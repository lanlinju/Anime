package com.lanlinju.animius.presentation.screen.main

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lanlinju.animius.BuildConfig
import com.lanlinju.animius.R
import com.lanlinju.animius.application.AnimeApplication
import com.lanlinju.animius.util.CHECK_UPDATE_ADDRESS
import com.lanlinju.animius.util.KEY_IS_AUTO_CHECK_UPDATE
import com.lanlinju.animius.util.KEY_LAST_CHECK_UPDATE_TIME
import com.lanlinju.animius.util.installApk
import com.lanlinju.animius.util.log
import com.lanlinju.animius.util.preferenceForBoolean
import com.lanlinju.animius.util.preferenceForLong
import com.lanlinju.animius.util.preferences
import com.lanlinju.animius.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    // 显示版本更新对话框的状态
    private val _showVersionUpdateDialog: MutableStateFlow<Boolean> =
        MutableStateFlow(value = false)
    val showVersionUpdateDialog = _showVersionUpdateDialog.asStateFlow()

    // 应用的偏好设置
    private val preferences = AnimeApplication.getInstance().preferences

    // 更新相关数据
    lateinit var updateDownloadUrl: String
    lateinit var updateDescription: String
    lateinit var updateVersionName: String
    lateinit var gitHubRelease: GitHubRelease

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val isAutoCheckUpdate =
                preferenceForBoolean(KEY_IS_AUTO_CHECK_UPDATE, true) // 是否启用自动检查更新
            if (isAutoCheckUpdate && shouldCheckForUpdates()) {
                // 更新上次检查时间为当前时间
                updateLastCheckTime()
                // 开始检查更新
                checkForUpdates()
            }
        }
    }

    /**
     * 检查是否需要更新
     * @return true 如果距离上次检查更新已经超过三天
     */
    private fun shouldCheckForUpdates(): Boolean {
        val currentTime = currentTimeMillis()
        val lastCheckTime = preferenceForLong(KEY_LAST_CHECK_UPDATE_TIME, 0) // 上次检查更新的时间（时间戳）
        val diffInMillis = currentTime - lastCheckTime
        // 将毫秒差转换为天数
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        return diffInDays >= 3
    }

    /**
     * 执行更新检查逻辑
     */
    private suspend fun checkForUpdates() {
        try {
            // 从 GitHub 获取更新信息
            gitHubRelease = httpClient.get(CHECK_UPDATE_ADDRESS).body<GitHubRelease>()

            // 保存更新信息
            this.updateDownloadUrl = gitHubRelease.assets[0].browserDownloadUrl
            this.updateDescription = gitHubRelease.body
            this.updateVersionName = gitHubRelease.tagName

            // 获取当前应用版本号
            val curVersionName = "v${BuildConfig.VERSION_NAME}"
            // 判断是否为新版本
            val isUpdateVersion = updateVersionName != curVersionName

            // 更新对话框状态
            _showVersionUpdateDialog.value = isUpdateVersion
        } catch (e: Exception) {
            e.log()
        }
    }

    /**
     * 下载更新文件并保存到本地
     */
    fun downloadUpdate(context: Context) {
        viewModelScope.launch {
            try {
                context.toast(R.string.downloading_updates)
                val file = File(context.getExternalFilesDir("apk/"), "base.apk")
                downloadApk(updateDownloadUrl, file)
                context.installApk(file)// 安装Apk
            } catch (_: Exception) {
                context.toast(R.string.download_software_failed)
            }
        }
    }

    /**
     * 下载 APK 文件
     * @param url 下载链接
     * @param file 保存路径
     */
    suspend fun downloadApk(url: String, file: File) {
        if (file.exists()) file.delete()

        httpClient.get(url) {
            onDownload { bytesSentTotal, contentLength ->
                "下载进度：${bytesSentTotal * 100 / contentLength}%".log()
            }
        }.bodyAsChannel().toByteArray().let {
            file.writeBytes(it)
        }
    }

    /**
     * 更新上次检查时间
     */
    private fun updateLastCheckTime() {
        preferences.edit { putLong(KEY_LAST_CHECK_UPDATE_TIME, currentTimeMillis()) }
    }

    /**
     * 关闭版本更新对话框
     */
    fun dismissUpdateDialog() {
        _showVersionUpdateDialog.value = false
    }

    /**
     * GitHub Release 数据模型
     */
    @Serializable
    data class GitHubRelease(
        @SerialName("tag_name") val tagName: String,    // 版本号，例如 "v1.1.0"
        val name: String,                              // Release 名称
        val body: String,                              // 更新日志
        val assets: List<Asset>                       // 附件列表
    )

    @Serializable
    data class Asset(
        @SerialName("browser_download_url") val browserDownloadUrl: String  // 下载链接
    )

    /**
     * HttpClient 配置
     */
    private val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 15_000
            }

        }
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }

}