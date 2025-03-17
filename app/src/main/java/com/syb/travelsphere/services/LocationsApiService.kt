package com.syb.travelsphere.services

import com.google.gson.JsonArray
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApiService {

    @GET("search")
    fun fetchAddressSuggestions(
        @Query("q") query: String,
        @Query("format") format: String = "json"
    ): Call<JsonArray>

    @GET("search")
    fun fetchGeoLocation(
        @Query("q") address: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1
    ): Call<JsonArray>
}
