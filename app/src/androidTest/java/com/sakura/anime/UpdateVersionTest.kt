package com.sakura.anime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sakura.anime.util.DownloadManager
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpdateVersionTest {
    val UPDATE_ADDRESS = "https://api.github.com/repos/Lanlinju/Anime/releases/latest"

    @Test
    fun checkUpdate(): Unit = runBlocking {
        val json = DownloadManager.getHtml(UPDATE_ADDRESS)
        val obj = JSONObject(json)
        val latestVersionName = obj.getString("name")
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val curVersionName = appContext.packageManager
            .getPackageInfo(appContext.packageName, 0).versionName
        val downloadUrl = obj.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")
        println(downloadUrl)
        println(obj.getString("body"))
        Assert.assertEquals(curVersionName, latestVersionName)
    }

    @Test
    fun getVersionName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val versionName = appContext.packageManager
            .getPackageInfo(appContext.packageName, 0).versionName
        println("v$versionName")
    }

}