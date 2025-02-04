package com.syb.travelsphere.utils

import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.GeoPoint

object GeoUtils {
    fun generateGeoHash(geoPoint: GeoPoint): String {
        return GeoFireUtils.getGeoHashForLocation(
            GeoLocation(geoPoint.latitude, geoPoint.longitude)
        )
    }
}