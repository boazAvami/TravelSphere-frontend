package com.syb.travelsphere.model.converters

import androidx.room.TypeConverter
import com.google.firebase.firestore.GeoPoint

class GeoPointConverter {
    @TypeConverter
    fun fromGeoPoint(geoPoint: GeoPoint?): String {
        return if (geoPoint != null) "${geoPoint.latitude},${geoPoint.longitude}" else ""
    }

    @TypeConverter
    fun toGeoPoint(data: String?): GeoPoint {
        return try {
            val parts = data?.split(",") ?: listOf("0.0", "0.0")
            GeoPoint(parts[0].toDouble(), parts[1].toDouble())
        } catch (e: Exception) {
            GeoPoint(0.0, 0.0) // Default to (0,0) if parsing fails
        }
    }


}
