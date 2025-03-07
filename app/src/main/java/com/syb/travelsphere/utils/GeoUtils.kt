package com.syb.travelsphere.utils

import android.location.Location
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.GeoPoint
import kotlin.math.*

object GeoUtils {
    private const val EARTH_RADIUS_KM = 6371.0

    fun generateGeoHash(geoPoint: GeoPoint): String {
        return GeoFireUtils.getGeoHashForLocation(
            GeoLocation(geoPoint.latitude, geoPoint.longitude)
        )
    }

    fun isWithinRadius(center: GeoPoint, point: GeoPoint, radiusInKm: Double): Boolean {
        val results = FloatArray(1)

        // Calculate distance using Android's Location API
        Location.distanceBetween(
            center.latitude, center.longitude,
            point.latitude, point.longitude,
            results
        )

        // Convert result from meters to kilometers
        return results[0] / 1000 <= radiusInKm
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
        val earthRadius = 6371000.0 // Earth radius in meters

        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c // Distance in meters
    }
}