package com.syb.travelsphere.model.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class PrimitiveTypeConverter {
    private val gson = Gson()

    // 🔹 Convert JSON String to List<String>
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val type: Type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // 🔹 Convert List<String> to JSON String
    @TypeConverter
    fun toString(list: List<String>?): String {
        return gson.toJson(list ?: emptyList<String>()) // ✅ Explicit type added
    }
}