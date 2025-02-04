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
    val title: String,
    val description: String,
    val imageUrl: String,
    val location: GeoPoint,  // Stores lat/lng
    val geoHash: String,      // Stores the hashed location
    val creationTime: Timestamp,
    val ownerId: String // References the `id` in the User table
) {
    companion object {
        const val ID_KEY = "id"
        const val TITLE_KEY = "title"
        const val DESCRIPTION_KEY = "description"
        const val IMAGE_URL_KEY = "imageUrl"
        const val LOCATION_KEY = "location"
        const val GEOHASH_KEY = "geoHash"
        const val CREATION_TIME_KEY = "creationTime"
        const val OWNER_ID_KEY = "ownerId"

        fun fromJSON(json: Map<String, Any>): Post {
            val id = json[ID_KEY] as? String ?: "" // type casting
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val imageUrl = json[IMAGE_URL_KEY] as? String ?: ""
            val location = json[LOCATION_KEY] as? GeoPoint ?: GeoPoint(0.0, 0.0)
            val geoHash = json[GEOHASH_KEY] as? String ?: generateGeoHash(location)
            val creationTime = json[CREATION_TIME_KEY] as? Timestamp ?: Timestamp(0, 0)
            val ownerId = json[OWNER_ID_KEY] as? String ?: "Unknown"
            val title = json[TITLE_KEY] as? String ?: ""

            return Post(
                id = id,
                title = title,
                description = description,
                imageUrl = imageUrl,
                location = location,
                geoHash = geoHash,
                creationTime = creationTime,
                ownerId = ownerId,
            )
        }
    }

    val json: HashMap<String, Any?> //TODO: Why need ANY? and not just Any without the '?'
        get() {
            return hashMapOf(
                ID_KEY to id,
                TITLE_KEY to title,
                DESCRIPTION_KEY to description,
                IMAGE_URL_KEY to imageUrl,
                LOCATION_KEY to location,
                GEOHASH_KEY to geoHash,  // Added GeoHash for Firestore queries
                CREATION_TIME_KEY to creationTime,
                OWNER_ID_KEY to ownerId
            )
        }
}
