package com.sakura.download.utils

import android.util.Log

internal var LOG_ENABLE = false

internal const val LOG_TAG = "Download"

internal fun <T> T.log(prefix: String = ""): T {
    val prefixStr = if (prefix.isEmpty()) "" else "[$prefix] "
    if (LOG_ENABLE) {
        if (this is Throwable) {
            Log.w(LOG_TAG, prefixStr + this.message, this)
        } else {
            Log.d(LOG_TAG, prefixStr + toString())
        }
    }
    return this
}