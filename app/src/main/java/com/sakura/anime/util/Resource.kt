package com.sakura.anime.util

sealed class Resource<T>(
    val data: T? = null,
    val error: Throwable? = null
) {
    class Success<T>(data: T) : Resource<T>(data = data)
    class Loading<T>(data: T? = null) : Resource<T>(data = data)
    class Error<T>(data: T? = null, error: Throwable? = null) :
        Resource<T>(data = data, error = error)
}