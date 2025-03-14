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

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostById(id: String): Post

    @Update
    fun updatePost(post: Post)

    @Query("DELETE FROM posts WHERE id = :postId")
    fun deletePostById(postId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(vararg post: Post)

    @Query("SELECT * FROM posts WHERE ownerId = :ownerId")
    fun getPostsByUser(ownerId: String): LiveData<List<Post>>
}
