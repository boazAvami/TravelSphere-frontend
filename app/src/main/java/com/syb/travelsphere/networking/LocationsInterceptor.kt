package com.syb.travelsphere.networking

import okhttp3.Response
import okhttp3.Interceptor

class LocationsInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .build()

        return chain.proceed(request)
    }
}
