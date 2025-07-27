package com.gauravbajaj.employeesdirectory.data

sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(
        val exception: ApiException,
        val message: String = exception.userMessage
    ) : ApiResult<T>()

    class Loading<T> : ApiResult<T>()
}

sealed class ApiException(
    val userMessage: String,
    val technicalMessage: String,
    cause: Throwable? = null
) : Exception(technicalMessage, cause) {

    data class NetworkException(
        val originalException: Throwable
    ) : ApiException(
        userMessage = "Please check your internet connection and try again",
        technicalMessage = "Network error: ${originalException.message}",
        cause = originalException
    )

    data class ServerException(
        val code: Int,
        val serverMessage: String
    ) : ApiException(
        userMessage = when (code) {
            in 500..599 -> "Server is temporarily unavailable. Please try again later"
            404 -> "Employee data not found"
            else -> "Something went wrong. Please try again"
        },
        technicalMessage = "Server error $code: $serverMessage"
    )

    data class ParseException(
        val originalException: Throwable
    ) : ApiException(
        userMessage = "Unable to load employee data. Please try again",
        technicalMessage = "JSON parsing error: ${originalException.message}",
        cause = originalException
    )

    data class UnknownException(
        val originalException: Throwable
    ) : ApiException(
        userMessage = "An unexpected error occurred. Please try again",
        technicalMessage = "Unknown error: ${originalException.message}",
        cause = originalException
    )
}