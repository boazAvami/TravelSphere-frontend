package com.syb.travelsphere.networking

import com.syb.travelsphere.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = BuildConfig.OPENSTREAM_MAP_URL

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(LocationsInterceptor())
            .build()
    }

    val locationsApiClient: LocationApi by lazy {
        val retrofitClient = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Add this line
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofitClient.create(LocationApi::class.java)
    }
}
