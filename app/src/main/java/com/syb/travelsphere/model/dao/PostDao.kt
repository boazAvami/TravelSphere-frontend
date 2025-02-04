package com.syb.travelsphere.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.model.User

@Dao
interface PostDao {
    @Query("SELECT * FROM posts")
    fun getAllPosts(): List<Post>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostById(id: String): Post

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(post: Post)

    @Update
    fun updatePost(post: Post)

    @Delete
    fun deletePost(post: Post)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertPost(post: Post)

    @Query("SELECT * FROM posts WHERE ownerId = :ownerId")
    suspend fun getPostsByUser(ownerId: Int): List<Post>
}
