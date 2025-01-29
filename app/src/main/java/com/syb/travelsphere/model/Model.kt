package com.syb.travelsphere.model

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import com.syb.travelsphere.model.dao.AppLocalDb
import com.syb.travelsphere.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

typealias UsersCallback = (List<User>) -> Unit //TODO: Move to the constant
typealias PostsCallback = (List<Post>) -> Unit
typealias EmptyCallback = () -> Unit

class Model private constructor() {
    private val database: AppLocalDbRepository = AppLocalDb.database // Mono state pattern
    private val executor = Executors.newSingleThreadExecutor() // Background thread
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper()) // Main thread

    private val firebaseModel = FirebaseModel()

    companion object {
        val shared = Model()
    }

    // User Functions.

    fun getAllUsers(callback: UsersCallback) {
        firebaseModel.getAllUsers(callback)
//        executor.execute {
//            try {
//                val users = database.userDao().getAllUsers().toMutableList()
//                mainHandler.post {
//                    callback(users)
//                }
//            } catch (e: Exception) {
//                Log.e("Model", "Error fetching users: ${e.message}")
//            }
//        }
    }

    fun addUser(user: User, callback: EmptyCallback) {
        firebaseModel.addUser(user, callback)
//        executor.execute {
//            try {
//                database.userDao().insertUser(user)
//                Log.d("Model", "Saved user: $user")
//                mainHandler.post {
//                    callback()
//                }
//            } catch (e: Exception) {
//                Log.e("Model", "Error saving user: ${e.message}")
//            }
//        }
    }

    fun editUser(user: User, callback: EmptyCallback) {
        firebaseModel.editUser(user, callback)
//        executor.execute {
//            try {
//                database.userDao().updateUser(user)
//                Log.d("Model", "Updated user: $user")
//                mainHandler.post {
//                    callback()
//                }
//            } catch (e: Exception) {
//                Log.e("Model", "Error updating user: ${e.message}")
//            }
//        }
    }

    fun deleteUser(user: User, callback: EmptyCallback) {
        firebaseModel.deleteUser(user.id.toString(), callback)
//        executor.execute {
//            try {
//                database.userDao().deleteUser(user)
//                Log.d("Model", "Deleted user: $user")
//                mainHandler.post {
//                    callback()
//                }
//            } catch (e: Exception) {
//                Log.e("Model", "Error deleting user: ${e.message}")
//            }
//        }
    }

    // Post Functions.

    fun getAllPosts(callback: PostsCallback) {
        executor.execute {
            try {
                val posts = database.postDao().getAllPosts().toMutableList()
                mainHandler.post {
                    callback(posts)
                }
            } catch (e: Exception) {
                Log.e("Model", "Error fetching posts: ${e.message}")
            }
        }
    }

    fun addPost(post: Post, callback: EmptyCallback) {
        executor.execute {
            try {
                database.postDao().insertPost(post)
                Log.d("Model", "Saved post: $post")
                mainHandler.post {
                    callback()
                }
            } catch (e: Exception) {
                Log.e("Model", "Error saving post: ${e.message}")
            }
        }
    }

    fun editPost(post: Post, callback: EmptyCallback) {
        executor.execute {
            try {
                database.postDao().updatePost(post)
                Log.d("Model", "Updated post: $post")
                mainHandler.post {
                    callback()
                }
            } catch (e: Exception) {
                Log.e("Model", "Error updating post: ${e.message}")
            }
        }
    }

    fun deletePost(post: Post, callback: EmptyCallback) {
        executor.execute {
            try {
                database.postDao().deletePost(post)
                Log.d("Model", "Deleted post: $post")
                mainHandler.post {
                    callback()
                }
            } catch (e: Exception) {
                Log.e("Model", "Error deleting post: ${e.message}")
            }
        }
    }
}
