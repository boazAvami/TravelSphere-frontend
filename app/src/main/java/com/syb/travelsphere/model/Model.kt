package com.syb.travelsphere.model

import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.base.BitmapCallback
import com.syb.travelsphere.base.EmptyCallback
import com.syb.travelsphere.base.ImageCallback
import com.syb.travelsphere.base.PostCallback
import com.syb.travelsphere.base.UserCallback
import com.syb.travelsphere.model.dao.AppLocalDb
import com.syb.travelsphere.model.dao.AppLocalDbRepository
import com.syb.travelsphere.utils.GeoUtils
import java.util.concurrent.Executors


class Model private constructor() {

    enum class LoadingState {
        LOADING,
        LOADED
    }
    private val database: AppLocalDbRepository = AppLocalDb.database // Mono state pattern
    private val executor = Executors.newSingleThreadExecutor() // Background thread
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper()) // Main thread

    val users: LiveData<List<User>> = database.userDao().getAllUsers()
    val posts: LiveData<List<Post>> = database.postDao().getAllPosts()

    private val _geoHashBounds = MutableLiveData<Pair<String, String>>() // Stores min & max geohash
    val geoHashBounds: LiveData<Pair<String, String>> = _geoHashBounds
    private val _users = MediatorLiveData<List<User>>()
    val nearbyUsers: LiveData<List<User>> get() = _users

    init {
        _users.addSource(geoHashBounds) { bounds ->
            _users.value = database.userDao().getNearbyUsers(bounds.first, bounds.second).value
        }
    }


    fun updateGeoHashBounds(currentLocation: GeoPoint, radiusInKm: Double) {
        val (minGeoHash, maxGeoHash) = GeoUtils.getGeoHashRange(currentLocation, radiusInKm)
        _geoHashBounds.postValue(Pair(minGeoHash, maxGeoHash)) // ✅ Update bounds
    }

//    val nearbyUsers: LiveData<List<User>> = database.userDao().getNearbyUsers(
//        minGeoHash = "",
//        maxGeoHash = ""
//    )

    val loadingState: MutableLiveData<LoadingState> = MutableLiveData<LoadingState>()
    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()

    companion object {
        val shared = Model()
        private const val TAG = "Model"
    }

    // User Functions.
    fun getNearbyUsers(currentLocation: GeoPoint, radiusInKm: Double): LiveData<List<User>> {
        val geoHashBounds = GeoUtils.getGeoHashRange(currentLocation, radiusInKm)
        return database.userDao().getNearbyUsers(geoHashBounds.first, geoHashBounds.second)
    }

    fun getUserById(userId: String, callback: UserCallback) {
        loadingState.postValue(LoadingState.LOADING)

        try {
            firebaseModel.getUserById(userId) {
                    loadingState.postValue(LoadingState.LOADED)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users: ${e.message}")
        }

    }

    fun refreshNearbyUsers(currentLocation: GeoPoint, radiusInKm: Double) {
        loadingState.postValue(LoadingState.LOADING)

        var lastUpdated: Long = User.lastUpdated
        val geoHashBounds = GeoUtils.getGeoHashRange(currentLocation, radiusInKm)

        firebaseModel.getNearbyUsers(
            currentLocation = currentLocation,
            radiusInKm = radiusInKm,
            sinceLastUpdated = lastUpdated
        ) { usersList ->

            executor.execute {
                var currentTime = lastUpdated

                for (user in usersList) {
                    database.userDao().insertUser(user)
                    user.lastUpdated?.let {
                        if (currentTime  < it) {
                            currentTime = it
                        }
                    }
                }

                User.lastUpdated = currentTime
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun getAllUsers() {
        loadingState.postValue(LoadingState.LOADING)
        val lastUpdated: Long = User.lastUpdated

        firebaseModel.getAllUsers(0L) { usersList ->
            executor.execute {
                var currentTime = lastUpdated

                for (user in usersList) {
                    database.userDao().insertUser(user)
                    user.lastUpdated?.let {
                        if (currentTime  < it) {
                            currentTime = it
                        }
                    }
                }

                User.lastUpdated = currentTime
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun refreshAllUsers() {
        loadingState.postValue(LoadingState.LOADING)
        val lastUpdated: Long = User.lastUpdated

        firebaseModel.getAllUsers(lastUpdated) { usersList ->
            executor.execute {
                var currentTime = lastUpdated

                for (user in usersList) {
                    database.userDao().insertUser(user)
                    user.lastUpdated?.let {
                        if (currentTime  < it) {
                            currentTime = it
                        }
                    }
                }

                User.lastUpdated = currentTime
                loadingState.postValue(LoadingState.LOADED)
            }
        }
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
        loadingState.postValue(LoadingState.LOADING)
        try {
            firebaseModel.getPostById(postId) {
                    loadingState.postValue(LoadingState.LOADED)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user: ${e.message}")
        }
    }

    fun getPostsByUserId(ownerUserId: String, callback: (LiveData<List<Post>>) -> Unit) {
        loadingState.postValue(LoadingState.LOADING)

        try {
            var lastUpdated: Long = Post.lastUpdated

            firebaseModel.getPostsByUserId(ownerUserId, lastUpdated) { postsList ->
                Log.d(TAG, "firebase fetching: firebase contains ${postsList.size  ?: 0} posts")

                executor.execute {
                    val latestTime = lastUpdated

                    for (post in postsList) {
                        Log.d(TAG, "🔹 Attempting to insert post: ${post.id}") // Debugging insert

                        database.postDao().insertPost(post)

                        post.lastUpdated?.let {
                            if (latestTime < it) {
                                lastUpdated = it
                            }
                        }
                    }

                    Post.lastUpdated = latestTime
                    val posts = database.postDao().getAllPosts()
                    Log.d(TAG, "After fetching: Room contains ${posts.value?.size ?: 0} posts")

                    mainHandler.post {
                        callback(posts)
                    }
                    loadingState.postValue(LoadingState.LOADED)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users: ${e.message}")
        }
    }

    fun getAllPosts() {
        loadingState.postValue(LoadingState.LOADING)
        val lastUpdated: Long = Post.lastUpdated

        firebaseModel.getAllPosts(0L) { postsList ->
            executor.execute {
                var currentTime = lastUpdated

                for (post in postsList) {
                    val postOwner = database.userDao().getUserById(post.ownerId)
                    if (postOwner != null) {
                        database.postDao().insertPost(post)
                    } else {
                        firebaseModel.getUserById(post.ownerId) {
                            database.userDao().insertUser(postOwner)
                            database.postDao().insertPost(post)
                        }
                    }
                    post.lastUpdated?.let {
                        if (currentTime  < it) {
                            currentTime = it
                        }
                    }
                }

                Post.lastUpdated = currentTime
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }


    fun refreshAllPosts() {
        loadingState.postValue(LoadingState.LOADING)
        val lastUpdated: Long = Post.lastUpdated

        firebaseModel.getAllPosts(lastUpdated) { postsList ->
            executor.execute {
                var currentTime = lastUpdated

                for (post in postsList) {
                    val postOwner = database.userDao().getUserById(post.ownerId)
                    if (postOwner != null) {
                        database.postDao().insertPost(post)
                    } else {
                        firebaseModel.getUserById(post.ownerId) {
                            database.userDao().insertUser(postOwner)
                            database.postDao().insertPost(post)
                        }
                    }
                    post.lastUpdated?.let {
                        if (currentTime  < it) {
                            currentTime = it
                        }
                    }
                }

                Post.lastUpdated = currentTime
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun addPost(post: Post, images: List<Bitmap>?, callback: EmptyCallback) {
        val postRef = firebaseModel.generatePostReference() // ✅ Get Firestore-generated ID
        val postId = postRef.id // Retrieve the generated ID

        val newPost = post.copy(id = postId) // Assign Firestore ID to the post

        if (!images.isNullOrEmpty()) {
            val uploadedUrls = mutableListOf<String>()

            images.forEach { image ->
                uploadImage(image) { imageUrl ->
                    if (!imageUrl.isNullOrBlank()) {
                        uploadedUrls.add(imageUrl)
                    }

                    if (uploadedUrls.size == images.size) {
                        val updatedPost = post.copy(photos = uploadedUrls)
                        firebaseModel.addPost(updatedPost, postRef, callback)
                    }
                }
            }
        } else {
            firebaseModel.addPost(newPost, postRef, callback)
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

    fun getImageByUrl(imageUrl: String, callback: BitmapCallback) {
        cloudinaryModel.getImageByUrl(imageUrl, callback)
    }
}