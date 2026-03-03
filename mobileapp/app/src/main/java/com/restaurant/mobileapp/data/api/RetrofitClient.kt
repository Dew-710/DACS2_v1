package com.restaurant.mobileapp.data.api

import com.restaurant.mobileapp.BuildConfig
import com.restaurant.mobileapp.data.api.GsonConfig.converterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
        
        // Add token if available
        val token = TokenManager.getToken()
        if (token != null) {
            requestBuilder.addHeader("Authorization", token)
        }
        
        requestBuilder.addHeader("Content-Type", "application/json")
        requestBuilder.addHeader("Accept", "application/json")
        
        chain.proceed(requestBuilder.build())
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(converterFactory)
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

object TokenManager {
    private var token: String? = null
    
    fun setToken(newToken: String?) {
        token = newToken
    }
    
    fun getToken(): String? = token
    
    fun clearToken() {
        token = null
    }
}

