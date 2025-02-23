package com.syb.travelsphere.utils

import android.location.Location
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.GeoPoint

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
}