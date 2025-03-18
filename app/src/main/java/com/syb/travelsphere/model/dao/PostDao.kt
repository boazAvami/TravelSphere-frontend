package com.syb.travelsphere.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.syb.travelsphere.model.Post

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY creationTime DESC")
    fun getAllPosts(): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostById(id: String): Post

    @Update
    fun updatePost(post: Post)

    @Query("DELETE FROM posts WHERE id = :postId")
    fun deletePostById(postId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(vararg post: Post)

    @Query("SELECT * FROM posts WHERE ownerId = :ownerId ORDER BY creationTime DESC")
    fun getPostsByUser(ownerId: String): List<Post>
}
