package com.sakura.anime

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.navigation.compose.rememberNavController
import com.sakura.anime.presentation.navigation.AnimeNavHost
import com.sakura.anime.presentation.navigation.Screen
import com.sakura.anime.presentation.screen.crash.CrashActivity
import com.sakura.anime.presentation.theme.AnimeTheme
import com.sakura.anime.util.getCrashLogInfo
import com.sakura.anime.util.logCrashToFile
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // 设置全局异常捕获处理
        setGlobalExceptionHandler()

        enableEdgeToEdge()

        //https://github.com/android/compose-samples/issues/1256
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

        setContent {
            AnimeTheme {
                App()
            }
        }
    }

    private fun setGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            logCrashToFile(e)
            launchCrashActivity(e)
        }
    }

    private fun launchCrashActivity(e: Throwable) {
        val crashLog = getCrashLogInfo(e)
        val intent = Intent(this, CrashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("crash_log", crashLog)
        }
        startActivity(intent)
        finish()
        exitProcess(0)
    }
}

@Composable
private fun App(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    AnimeNavHost(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        navController = navController,
        onNavigateToAnimeDetail = { detailUrl, mode ->
            navController.navigate(route = Screen.AnimeDetailScreen.passUrl(detailUrl, mode))
        },
        onNavigateToVideoPlay = { episodeUrl, mode ->
            navController.navigate(route = Screen.VideoPlayScreen.passUrl(episodeUrl, mode))
        },
        onBackClick = {
            navController.popBackStack()
        },
        onNavigateToHistory = {
            navController.navigate(Screen.HistoryScreen.route)
        },
        onNavigateToDownload = {
            navController.navigate(Screen.DownloadScreen.route)
        },
        onNavigateToDownloadDetail = { detailUrl, title ->
            navController.navigate(Screen.DownloadDetailScreen.passUrl(detailUrl, title))
        },
        onNavigateToSearch = {
            navController.navigate(Screen.SearchScreen.route)
        },
        onNavigateToAppearance = {
            navController.navigate(Screen.AppearanceScreen.route)
        },
        onNavigateToDanmakuSettings = {
            navController.navigate(Screen.DanmakuSettingsScreen.route)
        },
    )
}



