package com.syb.travelsphere.utils

import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtil {
    fun formatTimestamp(timestamp: Timestamp): String {
        val instant = timestamp.toDate().toInstant() // Convert to Instant
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) // Convert to LocalDateTime
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // Define format

        return localDateTime.format(formatter)
    }
}