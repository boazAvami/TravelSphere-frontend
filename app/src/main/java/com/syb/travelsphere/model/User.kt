package com.syb.travelsphere.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.base.MyApplication
import com.syb.travelsphere.model.Post.Companion.LOCATION_KEY
import com.syb.travelsphere.utils.GeoUtils.generateGeoHash

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    var profilePictureUrl: String?,
    var userName: String,
    val location: GeoPoint? = null,
    val geoHash: String = location?.let { generateGeoHash(it) } ?: "", // Generate GeoHash
    var phoneNumber: String?,
    var isLocationShared: Boolean? = false,
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
        const val USERNAME_KEY = "userName"
        const val IS_LOCATION_SHARED_KEY = "isLocationShared"
        const val PROFILE_PICTURE_URL_KEY = "profilePictureUrl"
        const val LOCATION_KEY = "location"
        const val GEOHASH_KEY = "geoHash"
        const val PHONE_NUMBER_KEY = "phoneNumber"
        const val LAST_UPDATED_KEY = "lastUpdated"
        const val LOCAL_LAST_UPDATED_KEY = "localUserLastUpdated"


        fun fromJSON(json: Map<String, Any>): User {
            val id = json[ID_KEY] as? String ?: "" // type casting
            val userName = json[USERNAME_KEY] as? String ?: "Unknown"
            val isLocationShared = json[IS_LOCATION_SHARED_KEY] as? Boolean ?: false
            val profilePictureUrl = json[PROFILE_PICTURE_URL_KEY] as? String ?: ""
            val location = json[LOCATION_KEY] as? GeoPoint ?: GeoPoint(0.0, 0.0)
            val geoHash = json[GEOHASH_KEY] as? String ?: generateGeoHash(location)
            val phoneNumber = json[PHONE_NUMBER_KEY] as? String ?: ""
            val timestamp = json[LAST_UPDATED_KEY] as? Timestamp
            val lastUpdatedLongTimestamp = timestamp?.toDate()?.time

            return User(
                id = id,
                profilePictureUrl = profilePictureUrl,
                userName = userName,
                location = location,
                geoHash = geoHash,
                phoneNumber = phoneNumber,
                isLocationShared = isLocationShared,
                lastUpdated = lastUpdatedLongTimestamp
            )
        }
    }

    val json: HashMap<String, Any?> //TODO: Why need ANY? and not just Any without the '?'
        get() {
            return hashMapOf(
                ID_KEY to id,
                IS_LOCATION_SHARED_KEY to isLocationShared,
                PROFILE_PICTURE_URL_KEY to profilePictureUrl,
                LOCATION_KEY to location,
                GEOHASH_KEY to geoHash,
                PHONE_NUMBER_KEY to phoneNumber,
                USERNAME_KEY to userName,
                LAST_UPDATED_KEY to FieldValue.serverTimestamp()
            )
        }
}