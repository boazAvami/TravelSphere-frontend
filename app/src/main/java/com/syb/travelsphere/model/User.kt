package com.syb.travelsphere.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profilePictureUrl: String,
    val userName: String,
    val password: String,
    val email: String,
    val phoneNumber: String,
    var isLocationShared: Boolean
)
