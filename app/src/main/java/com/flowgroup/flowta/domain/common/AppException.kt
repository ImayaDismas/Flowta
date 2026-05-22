package com.flowgroup.flowta.domain.common

sealed class AppException : Exception() {
    data class NetworkException(val code: Int, override val message: String) : AppException()
    data class ServerException(val code: Int, override val message: String) : AppException()
    data class LocalException(override val message: String) : AppException()
    data object NoConnectivityException : AppException()
    data object UnauthorizedException : AppException()
}
