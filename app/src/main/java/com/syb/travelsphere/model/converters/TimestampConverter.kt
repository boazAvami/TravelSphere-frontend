package com.syb.travelsphere.model.converters

import androidx.room.TypeConverter
import com.google.firebase.Timestamp

object TimestampConverter {
    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long? {
        return value?.seconds
    }

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it, 0) }
    }
}
