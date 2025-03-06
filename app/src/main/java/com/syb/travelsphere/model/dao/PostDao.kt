package com.syb.travelsphere.model.dao

import androidx.lifecycle.LiveData
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
    fun getAllPosts(): LiveData<List<Post>>

//    @Query("SELECT * FROM posts ORDER BY lastUpdated DESC")
//    fun getAllPostsOrderDesc(): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostById(id: String): Post

    @Update
    fun updatePost(post: Post)

    @Delete
    fun deletePost(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(vararg post: Post)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertPosts(posts: List<Post>)  // Batching insertions

    @Query("SELECT * FROM posts WHERE ownerId = :ownerId")
    fun getPostsByUser(ownerId: String): LiveData<List<Post>>
}
