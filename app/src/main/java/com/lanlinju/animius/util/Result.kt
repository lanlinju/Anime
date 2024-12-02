package com.lanlinju.animius.util

sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(val error: Throwable) : Result<Nothing>
}

inline fun <T, R> Result<T>.map(map: (T) -> R): Result<R> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> {
    return when (this) {
        is Result.Error -> {
            action(error)
            this
        }

        is Result.Success -> this
    }
}

fun <T> Result<T>.asEmptyDataResult(): EmptyResult {
    return map { }
}

typealias EmptyResult = Result<Unit>