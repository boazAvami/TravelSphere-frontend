package com.syb.travelsphere.services

import android.util.Log
import com.google.firebase.firestore.GeoPoint
import com.google.gson.JsonArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationsService {

    private val apiService = RetrofitClient.instance

    private fun <T> makeRequest(
        call: Call<T>,
        parseResponse: (T?) -> Any?,
        onSuccess: (Any?) -> Unit,
        onFailure: () -> Unit
    ) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    val parsedData = parseResponse(response.body())
                    if (parsedData != null) {
                        onSuccess(parsedData)
                    } else {
                        onFailure()
                    }
                } else {
                    onFailure()
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                Log.e(TAG, "API_ERROR: ${t.message}")
                onFailure()
            }
        })
    }

    // Fetch address suggestions
    fun fetchAddressSuggestions(query: String, callback: (List<String>?) -> Unit) {
        makeRequest(
            call = apiService.fetchAddressSuggestions(query),
            parseResponse = { jsonArray ->
                (jsonArray as? JsonArray)?.takeIf { it.size() > 0 }?.mapNotNull {
                    it.asJsonObject.get("display_name")?.asString
                }
            },
            onSuccess = { result -> callback(result as? List<String>) },
            onFailure = { callback(null) }
        )
    }

    // Fetch geo location for an address
    fun fetchGeoLocation(address: String, callback: (GeoPoint?) -> Unit) {
        makeRequest(
            call = apiService.fetchGeoLocation(address),
            parseResponse = { jsonArray ->
                (jsonArray as? JsonArray)?.takeIf { it.size() > 0 }?.let {
                    val obj = it[0].asJsonObject
                    val lat = obj.get("lat").asString.toDouble()
                    val lon = obj.get("lon").asString.toDouble()
                    GeoPoint(lat, lon)
                }
            },
            onSuccess = { result -> callback(result as? GeoPoint) },
            onFailure = { callback(null) }
        )
    }

    companion object {
        private const val TAG = "GeoLocationService"
    }
}