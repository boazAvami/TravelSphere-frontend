package com.syb.travelsphere.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.syb.travelsphere.base.EmptyCallback
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.model.User

@Dao
interface UserDao {

    @Query("DELETE FROM users")
    fun clearAllUsers()

    @Query("DELETE FROM users WHERE id NOT IN (:userIds)")
    fun deleteUsersNotInList(userIds: List<String>)


    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE geoHash BETWEEN :minGeoHash AND :maxGeoHash AND isLocationShared = 1")
    fun getUsersInGeoHashRange(minGeoHash: String, maxGeoHash: String): LiveData<List<User>>


    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(vararg user: User)

    @Update
    fun updateUser(user: User)

    @Query("DELETE FROM users WHERE id = :userId")
    fun deleteUserById(userId: String)
}