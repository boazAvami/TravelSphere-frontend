package com.syb.travelsphere.services

import android.util.Log
import com.google.firebase.firestore.GeoPoint
import okhttp3.*
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.syb.travelsphere.BuildConfig
import java.io.IOException

class LocationsService {

    private val client = OkHttpClient()

    private fun createRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .header("User-Agent", "YourAppName") // Required by Nominatim
            .build()
    }

    private fun <T> makeRequest(
        url: String,
        parseResponse: (String) -> T?,
        onSuccess: (T) -> Unit,
        onFailure: () -> Unit
    ) {
        val request = createRequest(url)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = responseBody.string()
                    try {
                        val result = parseResponse(json)
                        if (result != null) {
                            onSuccess(result)
                        } else {
                            onFailure()
                        }
                    } catch (e: JsonSyntaxException) {
                        Log.e(TAG, "API_ERROR JSON parsing error: ${e.message}")
                        onFailure()
                    }
                }
            }
        })
    }

    // Fetch address suggestions
    fun fetchAddressSuggestions(query: String, callback: (List<String>?) -> Unit) {
        val url = BuildConfig.OPENSTREAM_MAP_URL + query
        makeRequest(
            url,
            parseResponse = { json ->
                val jsonArray = JsonParser.parseString(json).asJsonArray
                val suggestions = mutableListOf<String>()
                jsonArray.forEach {
                    val obj = it.asJsonObject
                    val displayName = obj.get("display_name").asString
                    suggestions.add(displayName)
                }
                suggestions.takeIf { it.isNotEmpty() }
            },
            onSuccess = { result -> callback(result) },
            onFailure = { callback(null) }
        )
    }

    // Fetch geo location for an address
    fun fetchGeoLocation(address: String, callback: (GeoPoint?) -> Unit) {
        val url = BuildConfig.OPENSTREAM_MAP_URL + address + "&limit=1"
        makeRequest(
            url,
            parseResponse = { json ->
                val jsonArray = JsonParser.parseString(json).asJsonArray
                if (jsonArray.size() > 0) {
                    val obj = jsonArray[0].asJsonObject
                    val lat = obj.get("lat").asString.toDouble()
                    val lon = obj.get("lon").asString.toDouble()
                    GeoPoint(lat, lon)
                } else {
                    null
                }
            },
            onSuccess = { result -> callback(result) },
            onFailure = { callback(null) }
        )
    }

    companion object {
        private const val TAG = "GeoLocationService"
    }
}