package com.sakura.anime.presentation.screen.week

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.sakura.anime.BuildConfig
import com.sakura.anime.R
import com.sakura.anime.data.remote.dto.AnimeBean
import com.sakura.anime.domain.repository.AnimeRepository
import com.sakura.anime.util.CHECK_UPDATE_ADDRESS
import com.sakura.anime.util.DownloadManager
import com.sakura.anime.util.KEY_DOWNLOAD_UPDATE_URL
import com.sakura.anime.util.Resource
import com.sakura.anime.work.UpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class WeekViewModel @Inject constructor(
    private val repository: AnimeRepository,
) : ViewModel() {
    private val _weekDataMap: MutableStateFlow<Resource<Map<Int, List<AnimeBean>>>> =
        MutableStateFlow(value = Resource.Loading)
    val weekDataMap: StateFlow<Resource<Map<Int, List<AnimeBean>>>> get() = _weekDataMap

    private val _isUpdateAvailable: MutableStateFlow<Boolean> = MutableStateFlow(value = false)
    val isUpdateAvailable: StateFlow<Boolean> get() = _isUpdateAvailable

    private val _isUpdateCheckInProgress: MutableStateFlow<Boolean> =
        MutableStateFlow(value = false)
    val isUpdateCheckInProgress: StateFlow<Boolean> get() = _isUpdateCheckInProgress

    private lateinit var updateDownloadUrl: String
    lateinit var updateDescription: String
    lateinit var updateVersionName: String

    init {
        getWeekData()
    }

    private fun getWeekData() {
        viewModelScope.launch {
            _weekDataMap.value = repository.getWeekData()
        }
    }

    fun refresh() {
        _weekDataMap.value = Resource.Loading
        getWeekData()
    }

    fun checkVersionUpdate(context: Context) {

        _isUpdateCheckInProgress.value = true

        viewModelScope.launch {
            try {
                val json = DownloadManager.getHtml(CHECK_UPDATE_ADDRESS)
                val obj = JSONObject(json)
                val downloadUpdateUrl =
                    obj.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")

                this@WeekViewModel.updateDownloadUrl = downloadUpdateUrl
                this@WeekViewModel.updateDescription = obj.getString("body")
                this@WeekViewModel.updateVersionName = obj.getString("tag_name")

                val latestVersionName = obj.getString("name")
                val curVersionName = BuildConfig.VERSION_NAME
                val isUpdateVersion = !latestVersionName.equals(curVersionName)

                _isUpdateCheckInProgress.value = false
                _isUpdateAvailable.value = isUpdateVersion
                if (!isUpdateVersion) {
                    val msg = context.getString(R.string.no_new_version)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                _isUpdateCheckInProgress.value = false
                val msg = context.getString(R.string.check_updates_failed)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun downloadVersionUpdate(context: Context, lifecycleOwner: LifecycleOwner) {
        dismissVersionUpdateDialog()

        val updateWorkRequest = OneTimeWorkRequestBuilder<UpdateWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(workDataOf(KEY_DOWNLOAD_UPDATE_URL to updateDownloadUrl))
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork("updateWork", ExistingWorkPolicy.REPLACE, updateWorkRequest)

        workManager.getWorkInfosForUniqueWorkLiveData("updateWork")
            .observe(lifecycleOwner) { listOfWorkInfo ->
                // If there are no matching work info, do nothing
                if (listOfWorkInfo == null || listOfWorkInfo.isEmpty()) {
                    return@observe
                }

                for (workInfo in listOfWorkInfo) {
                    if (workInfo.state.isFinished) {
                        if (workInfo.state == WorkInfo.State.FAILED) {
                            val errorMsg = context.getString(R.string.download_software_failed)
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        val msg = context.getString(R.string.downloading_updates)
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun dismissVersionUpdateDialog() {
        _isUpdateAvailable.value = false
    }

    fun dismissLoadingIndicationDialog() {
        _isUpdateCheckInProgress.value = false
    }
}