package com.flowgroup.flowta.domain.common

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: AppException) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    inline fun <R> fold(onSuccess: (T) -> R, onError: (AppException) -> R): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(exception)
    }
}

inline fun <T> resultOf(block: () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: AppException) {
    Result.Error(e)
} catch (e: Throwable) {
    Result.Error(AppException.LocalException(e.message ?: e::class.simpleName.orEmpty()))
}
