package com.syb.travelsphere.model

import android.content.Context
import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.base.BitmapCallback
import com.syb.travelsphere.base.EmptyCallback
import com.syb.travelsphere.base.ImageCallback
import com.syb.travelsphere.base.PostCallback
import com.syb.travelsphere.base.PostsCallback
import com.syb.travelsphere.base.UserCallback
import com.syb.travelsphere.base.UsersCallback
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
    private var _nearbyUsers = MutableLiveData<List<User>>()
    val nearbyUsers: LiveData<List<User>> = _nearbyUsers

    val loadingState: MutableLiveData<LoadingState> = MutableLiveData<LoadingState>()
    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()

    companion object {
        val shared = Model()
        private const val TAG = "Model"
    }

    fun fetchUsersByIds(userIds: List<String>, callback: UsersCallback) {
        val userMap = mutableMapOf<String, User>()
        val remainingUsers = userIds.toMutableSet() // Track missing users

        executor.execute {
            // Check Room database for cached users
            userIds.forEach { userId ->
                val cachedUser = database.userDao().getUserById(userId)
                if (cachedUser != null) {
                    userMap[userId] = cachedUser
                    remainingUsers.remove(userId) // Remove found users from the fetch list
                }
            }

            // If all users exist in Room, return them immediately
            if (remainingUsers.isEmpty()) {
                mainHandler.post { callback(userMap.values.toList()) }
                return@execute
            }

            // Fetch missing users from Firestore
            try {
                firebaseModel.getUsersByIds(remainingUsers.toList()) { fetchedUsers ->
                    fetchedUsers.forEach { user ->
                        userMap[user.id] = user
                        executor.execute {
                            database.userDao().insertUser(user) // Cache in Room
                        }
                    }

                    mainHandler.post { callback(userMap.values.toList()) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching users: ${e.message}")
            }
        }
    }

    fun getUserById(userId: String, callback: UserCallback) {
        loadingState.postValue(LoadingState.LOADING)

        try {
            firebaseModel.getUserById(userId) { user ->
                callback(user)
                loadingState.postValue(LoadingState.LOADED)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting post: ${e.message}")
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
        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user: ${e.message}")
        }
    }

    fun editUser(user: User, newProfilePicture: Bitmap?, context: Context, callback: EmptyCallback) {
        try {

            if (newProfilePicture != null) {
                // Delete the old image if it exists
                user.profilePictureUrl?.let { oldUrl ->
                    cloudinaryModel.deleteImage(oldUrl) {
                        Log.d(TAG, "editUser: image deleted")
                    }
                }

                // Upload the new image
                uploadImage(newProfilePicture) { newUrl ->
                    if (!newUrl.isNullOrBlank()) {
                        val updatedUser = user.copy(profilePictureUrl = newUrl)

                        if (user.isLocationShared == true) {
                            updatedUserWithLocation(
                                updatedUser,
                                context
                            ) { userWithUpdatedLocation ->
                                if (userWithUpdatedLocation != null) {
                                    Log.d(TAG, "editUser: $userWithUpdatedLocation")
                                    firebaseModel.editUser(userWithUpdatedLocation, callback)
                                }
                            }
                        } else {
                            firebaseModel.editUser(updatedUser) {
                                Log.d(TAG, "editUser: User updated without location change")
                                callback()
                            }
                        }
                    } else {
                        Log.d(TAG, "editUser: error uploading profile picture")
                        callback()
                    }
                }
            } else {
                if (user.isLocationShared == true) {
                    updatedUserWithLocation(user, context) { userWithUpdatedLocation ->
                        if (userWithUpdatedLocation != null) {
                            Log.d(TAG, "editUser: $userWithUpdatedLocation")
                            firebaseModel.editUser(userWithUpdatedLocation, callback)
                        }
                    }
                } else {
                    firebaseModel.editUser(user) {
                        Log.d(TAG, "editUser: User updated without location change")
                        callback()
                    }
                }

            }
        } catch (e: Exception) {
            Log.e(TAG, "Error editing users: ${e.message}")
        }
    }

    // Helper function to update user location and GeoHash
    private fun updatedUserWithLocation(user: User, context: Context, callback: UserCallback) {
        GeoUtils.getCurrentLocation(context) { geoPoint ->
            val newGeoHash = geoPoint?.let { GeoUtils.generateGeoHash(it) }
            Log.d(TAG, "Updating user with new GeoHash: $newGeoHash")

            val updatedUser = newGeoHash?.let { user.copy(location = geoPoint, geoHash = it) }
            callback(updatedUser)
        }
    }

    // Post Functions.

    fun getPostById(postId: String, callback: PostCallback) {
        loadingState.postValue(LoadingState.LOADING)
        try {
            firebaseModel.getPostById(postId) { post ->
                callback(post)
                loadingState.postValue(LoadingState.LOADED)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching post: ${e.message}")
        }
    }

    fun getPostsByUserId(ownerUserId: String, callback: PostsCallback) {
        loadingState.postValue(LoadingState.LOADING)

        try {
            var lastUpdated: Long = Post.lastUpdated

            firebaseModel.getPostsByUserId(ownerUserId, lastUpdated) { postsList ->
                Log.d(TAG, "firebase fetching: firebase contains ${postsList.size  ?: 0} posts")

                executor.execute {
                    val latestTime = lastUpdated

                    for (post in postsList) {
                        Log.d(TAG, "Attempting to insert post: ${post.id}") // Debugging insert

                        database.postDao().insertPost(post)

                        post.lastUpdated?.let {
                            if (latestTime < it) {
                                lastUpdated = it
                            }
                        }
                    }

                    val posts = database.postDao().getPostsByUser(ownerUserId)

                    mainHandler.post {
                        Log.d(TAG, "After fetching: Room contains ${posts.size ?: 0} posts")
                        callback(posts)
                    }

                    loadingState.postValue(LoadingState.LOADED)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching posts of user with id ${ownerUserId}: ${e.message}")
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
                    database.postDao().insertPost(post)
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
        try {
            val postRef = firebaseModel.generatePostReference() // Get Firestore-generated ID
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
        } catch (e: Exception) {
            Log.e(TAG, "Error adding post: ${e.message}")
        }
    }

    fun editPost(post: Post, callback: EmptyCallback) {
        try {
            firebaseModel.editPost(post, callback)
        } catch (e: Exception) {
            Log.e(TAG, "Error editing post : ${e.message}")
        }
    }

    fun deletePost(post: Post, callback: EmptyCallback) {
        var deletedImagesCount = 0

        try {
            if (post.photos.isEmpty()) {
                firebaseModel.deletePost(post.id, callback) // No images, delete post immediately
                return
            }

            post.photos.forEach { imageUrl ->
                cloudinaryModel.deleteImage(imageUrl) { success ->
                    if (!success) {
                        Log.e(TAG, "Failed to delete post image: $imageUrl")
                    }
                    deletedImagesCount++

                    // Delete the post only after all images are processed
                    if (deletedImagesCount == post.photos.size) {
                        firebaseModel.deletePost(post.id) {
                            executor.execute {
                                database.postDao().deletePostById(post.id)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post: ${e.message}")
        }
    }

    private fun uploadImage(image: Bitmap, callback: ImageCallback) {
        try {
            cloudinaryModel.uploadImage(image, callback)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading Image: ${e.message}")
        }
    }

    fun getImageByUrl(imageUrl: String, callback: BitmapCallback) {
        try {
            cloudinaryModel.getImageByUrl(imageUrl, callback)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting image: ${e.message}")
        }
    }

    private val GEO_HASH_ACCURACY = 1.2
    fun refreshAllNearbyUsers(location: GeoPoint, radius: Double, callback: EmptyCallback) {
        loadingState.postValue(LoadingState.LOADING)
        try {
            firebaseModel.getNearbyUsers(
                currentLocation = location,
                radiusInKm = radius * GEO_HASH_ACCURACY
            ) { usersList ->
                executor.execute {
                    database.runInTransaction {
                        // TODO: delete only not included users
//                        database.userDao().clearAllUsers()
                        for (user in usersList) {
                            database.userDao().insertUser(user)
                            Log.d("InsertedUserCheck", "Inserted User: ${user.userName}, Geohash: ${user.geoHash}")
                        }
                    }

                    mainHandler.post {
                        val (minGeoHash, maxGeoHash) = GeoUtils.getGeoHashRange(location, radius)
                        // Observe Room
                        val updatedUsers = database.userDao().getUsersInGeoHashRange(minGeoHash, maxGeoHash)
                        updatedUsers.observeForever { users ->
                            _nearbyUsers.postValue(users)  // Ensure LiveData update
                            Log.d(TAG, "Users in Room DB after update: ${users.map { it.userName }}")
                        }
                    }
                    loadingState.postValue(LoadingState.LOADED)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching nearby users: ${e.message}")
        }
    }

    fun getNearbyUsers(location: GeoPoint, radius: Double): LiveData<List<User>> {
        val geohashRange = calculateGeohashRange(location, radius) // Calculate geohash range
        return database.userDao().getUsersInGeoHashRange(geohashRange.first, geohashRange.second)
    }

    private fun calculateGeohashRange(location: GeoPoint, radius: Double): Pair<String, String> {
        val (minGeoHash, maxGeoHash) = GeoUtils.getGeoHashRange(location, radius)
        return Pair(minGeoHash, maxGeoHash)
    }

}