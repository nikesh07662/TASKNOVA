package com.academic.workflow.core.network

import com.academic.workflow.core.security.SecureStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val secureStorage: SecureStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = secureStorage.getAuthToken()

        val requestBuilder = original.newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        requestBuilder.header("Accept", "application/json")

        return chain.proceed(requestBuilder.build())
    }
}
