package com.syb.travelsphere.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var profilePictureUrl: String? = null,
    var userName: String? = null,
    var password: String,
    val email: String,
    var phoneNumber: String? = null,
    var isLocationShared: Boolean = false
) {
    companion object {

        const val ID_KEY = "id"
        const val USERNAME_KEY = "userName"
        const val IS_LOCATION_SHARED_KEY = "isLocationShared"
        const val PROFILE_PICTURE_URL_KEY = "profilePictureUrl"
        const val EMAIL_KEY = "email"
        const val PASSWORD_KEY = "password"
        const val PHONE_NUMBER_KEY = "phoneNumber"

        fun fromJson(json: Map<String, Any?>): User {
            val id = json[ID_KEY] as? Int ?: 0 // type casting
            val userName = json[USERNAME_KEY] as? String  ?: "Unknown"
            val isLocationShared = json[IS_LOCATION_SHARED_KEY] as? Boolean ?: false
            val profilePictureUrl = json[PROFILE_PICTURE_URL_KEY] as? String ?: ""
            val email = json[EMAIL_KEY] as? String ?: ""
            val password = json[PASSWORD_KEY] as? String ?: ""
            val phoneNumber = json[PHONE_NUMBER_KEY] as? String ?: ""

            return User(
                id = id,
                profilePictureUrl = profilePictureUrl,
                userName = userName,
                password = password,
                email = email,
                phoneNumber = phoneNumber,
                isLocationShared = isLocationShared
            )
        }
    }

    val json: HashMap<String, Any?> //TODO: Why need ANY? and not just Any without the '?'
        get() = hashMapOf(
            ID_KEY to id,
            PASSWORD_KEY to password,
            EMAIL_KEY to email,
            IS_LOCATION_SHARED_KEY to isLocationShared,
            PROFILE_PICTURE_URL_KEY to profilePictureUrl,
            PHONE_NUMBER_KEY to phoneNumber,
            USERNAME_KEY to userName
        )
}
