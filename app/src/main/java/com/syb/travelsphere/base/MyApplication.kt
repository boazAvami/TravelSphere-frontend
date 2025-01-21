package com.syb.travelsphere.base


import android.app.Application
import android.content.Context

// for app context to the localdb
class MyApplication: Application() {

    object Globals {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        Globals.context = applicationContext
    }
}