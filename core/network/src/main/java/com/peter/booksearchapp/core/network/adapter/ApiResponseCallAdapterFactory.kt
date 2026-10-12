package com.peter.booksearchapp.core.network.adapter

import com.peter.booksearchapp.core.network.ApiResponse
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class ApiResponseCallAdapterFactory(
    private val json: Json,
) : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null
        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) != ApiResponse::class.java) return null
        require(callType is ParameterizedType) { "ApiResponse는 ApiResponse<T> 형태로 선언해야 합니다." }

        val successType = getParameterUpperBound(0, callType)
        return ApiResponseCallAdapter<Any>(successType, json)
    }
}

private class ApiResponseCallAdapter<T : Any>(
    private val successType: Type,
    private val json: Json,
) : CallAdapter<T, Call<ApiResponse<T>>> {
    override fun responseType(): Type = successType

    override fun adapt(call: Call<T>): Call<ApiResponse<T>> = ApiResponseCall(call, successType, json)
}
