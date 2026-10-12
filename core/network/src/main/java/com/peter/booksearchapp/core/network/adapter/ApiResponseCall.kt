package com.peter.booksearchapp.core.network.adapter

import com.peter.booksearchapp.core.network.ApiResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type

internal class ApiResponseCall<T : Any>(
    private val delegate: Call<T>,
    private val successType: Type,
    private val json: Json,
) : Call<ApiResponse<T>> {
    override fun enqueue(callback: Callback<ApiResponse<T>>) {
        delegate.enqueue(
            object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    callback.onResponse(this@ApiResponseCall, Response.success(response.toApiResponse()))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    callback.onResponse(this@ApiResponseCall, Response.success(t.toApiResponse()))
                }
            },
        )
    }

    override fun execute(): Response<ApiResponse<T>> {
        val apiResponse = try {
            delegate.execute().toApiResponse()
        } catch (t: Throwable) {
            t.toApiResponse()
        }
        return Response.success(apiResponse)
    }

    @Suppress("UNCHECKED_CAST")
    private fun Response<T>.toApiResponse(): ApiResponse<T> {
        if (!isSuccessful) {
            return ApiResponse.Failure.HttpError(
                code = code(),
                errorMessage = errorBody()?.parseErrorMessage(),
                exception = HttpException(this),
            )
        }
        val body = body()
        return when {
            body != null -> ApiResponse.Success(body)
            successType == Unit::class.java -> ApiResponse.Success(Unit as T)
            else -> ApiResponse.Failure.UnknownError(IllegalStateException("응답 본문이 비어 있습니다. (HTTP ${code()})"))
        }
    }

    private fun Throwable.toApiResponse(): ApiResponse<T> = when (this) {
        is IOException -> ApiResponse.Failure.NetworkError(this)
        else -> ApiResponse.Failure.UnknownError(this)
    }

    private fun ResponseBody.parseErrorMessage(): String? = runCatching {
        json.decodeFromString<ErrorBody>(string()).message
    }.getOrNull()

    override fun clone(): Call<ApiResponse<T>> = ApiResponseCall(delegate.clone(), successType, json)

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() = delegate.cancel()

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()
}

@Serializable
private data class ErrorBody(
    val errorType: String? = null,
    val message: String? = null,
)
