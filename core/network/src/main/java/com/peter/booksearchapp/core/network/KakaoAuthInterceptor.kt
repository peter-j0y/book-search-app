package com.peter.booksearchapp.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

internal class KakaoAuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Authorization", "KakaoAK ${BuildConfig.KAKAO_API_KEY}")
            .build()
        return chain.proceed(request)
    }
}
