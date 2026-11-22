package com.academic.workflow.core.network

import com.academic.workflow.core.security.SecureStorage
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun create(baseUrl: String, secureStorage: SecureStorage): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Smart fallback interceptor:
        // Automatically redirects 127.0.0.1 / localhost to the Android emulator host gateway (10.0.2.2) if local connection fails.
        val smartFallbackInterceptor = Interceptor { chain ->
            val request = chain.request()
            try {
                chain.proceed(request)
            } catch (e: IOException) {
                val host = request.url.host
                if (host == "127.0.0.1" || host == "localhost") {
                    val fallbackUrl = request.url.newBuilder()
                        .host("10.0.2.2")
                        .build()
                    val newRequest = request.newBuilder().url(fallbackUrl).build()
                    try {
                        chain.proceed(newRequest)
                    } catch (e2: Exception) {
                        throw e
                    }
                } else {
                    throw e
                }
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(smartFallbackInterceptor)
            .addInterceptor(AuthInterceptor(secureStorage))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .build()

        val contentType = "application/json".toMediaType()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
