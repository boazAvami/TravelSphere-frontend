package com.syb.travelsphere.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.GeoPoint
import kotlin.math.*

object GeoUtils {
    private const val EARTH_RADIUS_KM = 6371.0
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var lastKnownLocation: GeoPoint? = null
    private const val  SIGNIFICANT_DISTANCE_THRESHOLD_KM = 0.5

    fun generateGeoHash(geoPoint: GeoPoint): String {
        return GeoFireUtils.getGeoHashForLocation(
            GeoLocation(geoPoint.latitude, geoPoint.longitude)
        )
    }

    fun getGeoHashRange(center: GeoPoint, radiusInKm: Double): Pair<String, String> {
        val geoHashQueryBounds = GeoFireUtils.getGeoHashQueryBounds(
            GeoLocation(center.latitude, center.longitude), radiusInKm * 1000
        )

        val minGeoHash = geoHashQueryBounds.first().startHash
        val maxGeoHash = geoHashQueryBounds.last().endHash

        return Pair(minGeoHash, maxGeoHash)
    }

    fun calculateDistance(start: GeoPoint, end: GeoPoint): Double {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM  * c // Distance in meters
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, callback: (GeoPoint?) -> Unit) {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

//        if (ActivityCompat.checkSelfPermission(
//                context, Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(
//                context, Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            fragment.requestPermissions(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ),
//                LOCATION_PERMISSION_REQUEST_CODE
//            )
//            Log.d("GeoUtils", "Requesting location permissions...")
//            return
//        }

        // Try last known location first (fastest)
        fusedLocationClient!!.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("GeoUtils", "Fast Last Known Location: Lat=${location.latitude}, Lon=${location.longitude}")
                callback(GeoPoint(location.latitude, location.longitude))
            } else {
                Log.d("GeoUtils", "No last known location available, forcing GPS update...")
                requestNewLocation(context, callback)
            }

//            requestNewLocation(context, callback)
            // Step 2: Request a real-time GPS update
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdates(1)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { freshLocation ->
                        val realTimeGeoPoint = GeoPoint(freshLocation.latitude, freshLocation.longitude)
                        Log.d("GeoUtils", "Real-time GPS: Lat=${freshLocation.latitude}, Lon=${freshLocation.longitude}")
                        callback(realTimeGeoPoint)
                    } ?: run {
                        Log.d("GeoUtils", "Real-time GPS update failed, using last known location.")
                    }
                }
            }

            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
        }.addOnFailureListener {
            Log.e("GeoUtils", "Failed to get last known location", it)
            callback(null)
        }
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocation(context: Context, callback: (GeoPoint?) -> Unit) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { freshLocation ->
                    val geoPoint = GeoPoint(freshLocation.latitude, freshLocation.longitude)
                    Log.d("GeoUtils", "Real-time GPS: Lat=${geoPoint.latitude}, Lon=${geoPoint.longitude}")
                    callback(geoPoint)
                } ?: run {
                    Log.d("GeoUtils", "Real-time GPS update failed.")
                    callback(null)
                }
            }
        }

        fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    // Continuous location observer
    fun observeLocationChanges(context: Context, callback: (GeoPoint) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
//            fragment.requestPermissions(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ),
//                LOCATION_PERMISSION_REQUEST_CODE
//            )
            Log.d("GeoUtils", "Requesting location permissions...")
            return
        }

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setMinUpdateIntervalMillis(500) // Updates every 1 seconds
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { newLocation ->
                    val newGeoPoint = GeoPoint(newLocation.latitude, newLocation.longitude)

                    // Check if location changed significantly (500m threshold)
                    if (lastKnownLocation == null || hasMovedSignificantly(lastKnownLocation!!, newGeoPoint, SIGNIFICANT_DISTANCE_THRESHOLD_KM)) {
                        Log.d("GeoUtils", "Significant location change detected! Updating UI...")
                        lastKnownLocation = newGeoPoint // Update last known location
                        callback(newGeoPoint) // Notify UI to update
                    } else {
//                        Log.d("GeoUtils", "Location changed but within 500m, not updating UI.")
                    }
                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback!!, null)
        Log.d("GeoUtils", "Started continuous location observation...")
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
            Log.d("GeoUtils", "Stopped location updates.")
        }
    }

    // Determines if a user has moved more than a threshold (in kilometers).
    private fun hasMovedSignificantly(oldLocation: GeoPoint, newLocation: GeoPoint, threshold: Double): Boolean {
        val distance = distanceBetween(oldLocation, newLocation)
        return distance > threshold
    }

    private fun distanceBetween(p1: GeoPoint, p2: GeoPoint): Double {
        val latDiff = p1.latitude - p2.latitude
        val lonDiff = p1.longitude - p2.longitude
        return sqrt(latDiff.pow(2) + lonDiff.pow(2)) * 111000 // Convert degrees to meters
    }

}