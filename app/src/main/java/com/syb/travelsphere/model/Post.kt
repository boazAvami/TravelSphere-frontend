package com.syb.travelsphere.model

import android.content.Context
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.base.MyApplication
import com.syb.travelsphere.model.User.Companion
import com.syb.travelsphere.model.User.Companion.LOCAL_LAST_UPDATED_KEY
import com.syb.travelsphere.model.converters.GeoPointConverter
import com.syb.travelsphere.model.converters.PrimitiveTypeConverter
import com.syb.travelsphere.utils.GeoUtils.generateGeoHash

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String,
    val description: String,

    @TypeConverters(PrimitiveTypeConverter::class)
    val photos: List<String>,
    val locationName : String,

    @TypeConverters(GeoPointConverter::class)
    val location: GeoPoint,  // Stores lat/lng
    val geoHash: String = location?.let { generateGeoHash(it) } ?: "", // Generate GeoHash
    val creationTime: Timestamp,
    val ownerId: String, // References the `id` in the User table
    val lastUpdated: Long? = null
) {
    companion object {
        var lastUpdated: Long
            get() = MyApplication.Globals.context?.getSharedPreferences(
                "TAG",
                Context.MODE_PRIVATE)
                ?.getLong(LOCAL_LAST_UPDATED_KEY, 0) ?: 0

            set(value) {
                MyApplication.Globals.context
                    ?.getSharedPreferences(
                        "TAG",
                        Context.MODE_PRIVATE
                    )?.apply {
                        edit().putLong(LOCAL_LAST_UPDATED_KEY, value).apply()
                    }
            }
        const val ID_KEY = "id"
        const val DESCRIPTION_KEY = "description"
        const val PHOTOS_KEY = "photos"
        const val LOCATION_NAME_KEY = "locationName"
        const val LOCATION_KEY = "location"
        const val GEOHASH_KEY = "geoHash"
        const val CREATION_TIME_KEY = "creationTime"
        const val OWNER_ID_KEY = "ownerId"
        const val LAST_UPDATED_KEY = "lastUpdated"
        const val LOCAL_LAST_UPDATED_KEY = "posts_last_updated"

        fun fromJSON(json: Map<String, Any>): Post {
            val id = json[ID_KEY] as? String ?: "" // type casting
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val photos = json[PHOTOS_KEY] as? List<String> ?: emptyList()
            val locationName = json[LOCATION_NAME_KEY] as? String ?: ""
            val location = json[LOCATION_KEY] as? GeoPoint ?: GeoPoint(0.0, 0.0)
            val geoHash = json[GEOHASH_KEY] as? String ?: generateGeoHash(location)
            val creationTime = json[CREATION_TIME_KEY] as? Timestamp ?: Timestamp(0, 0)
            val ownerId = json[OWNER_ID_KEY] as? String ?: "Unknown"
            val timestamp = json[User.LAST_UPDATED_KEY] as? Timestamp
            val lastUpdatedLongTimestamp = timestamp?.toDate()?.time

            return Post(
                id = id,
                description = description,
                photos = photos,
                location = location,
                geoHash = geoHash,
                creationTime = creationTime,
                ownerId = ownerId,
                locationName = locationName,
                lastUpdated = lastUpdatedLongTimestamp
            )
        }
    }

    val json: HashMap<String, Any?>
        get() {
            return hashMapOf(
                ID_KEY to id,
                DESCRIPTION_KEY to description,
                PHOTOS_KEY to photos,
                LOCATION_NAME_KEY to locationName,
                LOCATION_KEY to location,
                GEOHASH_KEY to geoHash,  // Added GeoHash for Firestore queries
                CREATION_TIME_KEY to creationTime,
                OWNER_ID_KEY to ownerId,
                LAST_UPDATED_KEY to FieldValue.serverTimestamp()
            )
        }
}