package com.peter.booksearchapp.core.network

import retrofit2.HttpException
import java.io.IOException

sealed interface ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>

    sealed interface Failure : ApiResponse<Nothing> {
        val exception: Throwable

        data class HttpError(
            val code: Int,
            val errorMessage: String?,
            override val exception: HttpException,
        ) : Failure

        data class NetworkError(override val exception: IOException) : Failure

        data class UnknownError(override val exception: Throwable) : Failure
    }
}

inline fun <T, R> ApiResponse<T>.map(transform: (T) -> R): ApiResponse<R> = when (this) {
    is ApiResponse.Success -> ApiResponse.Success(transform(data))
    is ApiResponse.Failure -> this
}
