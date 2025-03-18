package com.syb.travelsphere.utils

object PostEditTracker {
    var hasPostBeenModified = false

    fun markPostAsModified() {
        hasPostBeenModified = true
    }

    fun checkAndResetModifiedFlag(): Boolean {
        val wasModified = hasPostBeenModified
        hasPostBeenModified = false
        return wasModified
    }
}