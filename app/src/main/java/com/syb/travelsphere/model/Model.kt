package com.syb.travelsphere.model

import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.base.EmptyCallback
import com.syb.travelsphere.base.ImageCallback
import com.syb.travelsphere.base.PostCallback
import com.syb.travelsphere.base.PostsCallback
import com.syb.travelsphere.base.UserCallback
import com.syb.travelsphere.base.UsersCallback
import com.syb.travelsphere.model.dao.AppLocalDb
import com.syb.travelsphere.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class Model private constructor() {
    private val database: AppLocalDbRepository = AppLocalDb.database // Mono state pattern
    private val executor = Executors.newSingleThreadExecutor() // Background thread
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper()) // Main thread

    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()

    companion object {
        val shared = Model()
    }

    // User Functions.
    fun getUserById(userId: String, callback: UserCallback) {
        firebaseModel.getUserById(userId, callback)
    }

    fun getNearbyUsers(currentLocation: GeoPoint, radiusInKm: Double, callback: UsersCallback) {
        firebaseModel.getNearbyUsers(
            currentLocation = currentLocation, radiusInKm = radiusInKm, callback = callback
        )
    }

    fun getAllUsers(callback: UsersCallback) {
        firebaseModel.getAllUsers(callback)
    }

    fun addUser(user: User, image: Bitmap?, callback: EmptyCallback) {
        firebaseModel.addUser(user) {
            image?.let {
                uploadImage(image) { uri ->
                    if (!uri.isNullOrBlank()) {
                        val usr = user.copy(profilePictureUrl = uri)
                        firebaseModel.addUser(usr, callback)
                    } else {
                        callback()
                    }
                }
            } ?: callback()
        }
    }

    fun editUser(user: User, callback: EmptyCallback) {
        firebaseModel.editUser(user, callback)
    }

    // Post Functions.
    fun getPostById(postId: String, callback: PostCallback) {
        firebaseModel.getPostById(postId, callback)
    }

    fun getAllPosts(callback: PostsCallback) {
        firebaseModel.getAllPosts(callback)
    }

    fun addPost(post: Post, callback: EmptyCallback) {
        firebaseModel.addPost(post, callback)
    }

    fun editPost(post: Post, callback: EmptyCallback) {
        firebaseModel.editPost(post, callback)
    }

    fun deletePost(post: Post, callback: EmptyCallback) {
        firebaseModel.deletePost(post.id, callback)
    }

    private fun uploadImage(image: Bitmap, callback: ImageCallback) {
        cloudinaryModel.uploadImage(
            bitmap = image,
            callback = callback
        )
    }
}
