package com.syb.travelsphere.model

import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.base.BitmapCallback
import com.syb.travelsphere.base.BooleanCallback
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
        private const val TAG = "Model"
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

    fun editUser(user: User, newProfilePicture: Bitmap?, callback: EmptyCallback) {
        if (newProfilePicture != null) {
            // Delete the old image if it exists
            user.profilePictureUrl?.let { oldUrl ->
                cloudinaryModel.deleteImage(oldUrl) {}
            }

            // Upload the new image
            uploadImage(newProfilePicture) { newUrl ->
                if (!newUrl.isNullOrBlank()) {
                    val updatedUser = user.copy(profilePictureUrl = newUrl)
                    firebaseModel.editUser(updatedUser, callback)
                } else {
                    callback()
                }
            }
        } else {
            firebaseModel.editUser(user, callback)
        }
    }

    // Post Functions.
    fun getPostById(postId: String, callback: PostCallback) {
        firebaseModel.getPostById(postId, callback)
    }

    fun getAllPosts(callback: PostsCallback) {
        firebaseModel.getAllPosts(callback)
    }

    fun addPost(post: Post, images: List<Bitmap>?, callback: EmptyCallback) {
        if (!images.isNullOrEmpty()) {
            val uploadedUrls = mutableListOf<String>()

            images.forEach { image ->
                uploadImage(image) { imageUrl ->
                    if (!imageUrl.isNullOrBlank()) {
                        uploadedUrls.add(imageUrl)
                    }

                    if (uploadedUrls.size == images.size) {
                        val updatedPost = post.copy(photos = uploadedUrls)
                        firebaseModel.addPost(updatedPost, callback)
                    }
                }
            }
        } else {
            firebaseModel.addPost(post, callback)
        }
    }

    fun editPost(post: Post, callback: EmptyCallback) {
        firebaseModel.editPost(post, callback)
    }

    fun deletePost(post: Post, callback: EmptyCallback) {
        var deletedImagesCount = 0

        if (post.photos.isEmpty()) {
            firebaseModel.deletePost(post.id, callback) // No images, delete post immediately
            return
        }

        post.photos.forEach { imageUrl ->
            cloudinaryModel.deleteImage(imageUrl) { success ->
                if (!success) {
                    Log.e(TAG, "⚠️ Failed to delete post image: $imageUrl")
                }
                deletedImagesCount++

                // Delete the post only after all images are processed - to prevent
                if (deletedImagesCount == post.photos.size) {
                    firebaseModel.deletePost(post.id, callback)
                }
            }
        }
    }


    private fun uploadImage(image: Bitmap, callback: ImageCallback) {
        cloudinaryModel.uploadImage(image, callback)
    }

    private fun getImageByUrl(imageUrl: String, callback: BitmapCallback) {
        cloudinaryModel.getImageByUrl(imageUrl, callback)
    }
}
