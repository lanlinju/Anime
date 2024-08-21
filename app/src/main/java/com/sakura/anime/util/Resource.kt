package com.sakura.anime.util

sealed class Resource<out T>(
    val data: T? = null,
    val error: Throwable? = null
) {
    class Success<T>(data: T) : Resource<T>(data = data)
    object Loading : Resource<Nothing>()
    class Error(error: Throwable? = null) : Resource<Nothing>(error = error)
}