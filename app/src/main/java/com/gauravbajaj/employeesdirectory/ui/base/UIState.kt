package com.gauravbajaj.employeesdirectory.ui.base

import com.gauravbajaj.employeesdirectory.data.ApiException

/**
 * UIState represents the state of the UI for a given screen.
 *
 * It can be one of the following:
 *  - [Initial] - The initial state of the UI, before any data is loaded.
 *  - [Loading] - The loading state of the UI, while data is being loaded.
 *  - [Success] - The success state of the UI, when data is loaded successfully.
 *  - [Error] - The error state of the UI, when there is an error while loading data.
 *
 * The error state provides a user-friendly error message, a boolean indicating if the error is retryable,
 * and the underlying [ApiException].
 */
sealed class UIState<out T> {
    data object Initial : UIState<Nothing>()
    data object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
    data class Error(val message: String, val isRetryable: Boolean, val exception: ApiException) :
        UIState<Nothing>()

    companion object {
        fun <T> success(data: T) = Success(data)
        fun error(
            apiException: ApiException,
            allowRetry: Boolean = true,
            message: String? = null
        ): Error {
            return Error(
                message = message ?: apiException.userMessage,
                isRetryable = allowRetry && when (apiException) {
                    is ApiException.NetworkException -> true
                    is ApiException.ServerException -> apiException.code in 500..599
                    is ApiException.ParseException -> false
                    is ApiException.UnknownException -> true
                },
                exception = apiException
            )
        }
    }
}