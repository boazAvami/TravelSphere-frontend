package com.syb.travelsphere.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.utils.GeoUtils.generateGeoHash

@Entity(
    tableName = "posts",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["ownerId"],
        onDelete = ForeignKey.CASCADE
    )]
)

data class Post(
    @PrimaryKey val id: String,
    val description: String,
    val photos: List<String>,
    val locationName : String,
    val location: GeoPoint,  // Stores lat/lng
    val geoHash: String,      // Stores the hashed location
    val creationTime: Timestamp,
    val ownerId: String // References the `id` in the User table
) {
    companion object {
        const val ID_KEY = "id"
        const val DESCRIPTION_KEY = "description"
        const val PHOTOS_KEY = "photos"
        const val LOCATION_NAME_KEY = "locationName"
        const val LOCATION_KEY = "location"
        const val GEOHASH_KEY = "geoHash"
        const val CREATION_TIME_KEY = "creationTime"
        const val OWNER_ID_KEY = "ownerId"

        fun fromJSON(json: Map<String, Any>): Post {
            val id = json[ID_KEY] as? String ?: "" // type casting
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val photos = json[PHOTOS_KEY] as? List<String> ?: emptyList()
            val locationName = json[LOCATION_NAME_KEY] as? String ?: ""
            val location = json[LOCATION_KEY] as? GeoPoint ?: GeoPoint(0.0, 0.0)
            val geoHash = json[GEOHASH_KEY] as? String ?: generateGeoHash(location)
            val creationTime = json[CREATION_TIME_KEY] as? Timestamp ?: Timestamp(0, 0)
            val ownerId = json[OWNER_ID_KEY] as? String ?: "Unknown"

            return Post(
                id = id,
                description = description,
                photos = photos,
                location = location,
                geoHash = geoHash,
                creationTime = creationTime,
                ownerId = ownerId,
                locationName = locationName
            )
        }
    }

    val json: HashMap<String, Any?> //TODO: Why need ANY? and not just Any without the '?'
        get() {
            return hashMapOf(
                ID_KEY to id,
                DESCRIPTION_KEY to description,
                PHOTOS_KEY to photos,
                LOCATION_NAME_KEY to locationName,
                LOCATION_KEY to location,
                GEOHASH_KEY to geoHash,  // Added GeoHash for Firestore queries
                CREATION_TIME_KEY to creationTime,
                OWNER_ID_KEY to ownerId
            )
        }
}
