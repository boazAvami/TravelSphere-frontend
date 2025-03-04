package com.syb.travelsphere.model.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.syb.travelsphere.base.MyApplication
import com.syb.travelsphere.model.converters.GeoPointConverter
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.model.User
import com.syb.travelsphere.model.converters.PrimitiveTypeConverter
import com.syb.travelsphere.model.converters.TimestampConverter

@Database(entities = [User::class, Post::class], version = 1)
@TypeConverters(GeoPointConverter::class, TimestampConverter::class, PrimitiveTypeConverter::class) // Register the TypeConverter
abstract class AppLocalDbRepository: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
}
object AppLocalDb {

    // creates the db only on the fist time being called.
    val database: AppLocalDbRepository by lazy {

        val context = MyApplication.Globals.context ?: throw IllegalStateException("Application context is missing")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "dbFileName.db"
        )
//            .fallbackToDestructiveMigration() // just for development purposes
            .build()
    }
}