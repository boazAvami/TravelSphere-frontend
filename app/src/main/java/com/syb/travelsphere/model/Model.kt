package com.syb.travelsphere.model

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

    fun getUserById(userId: String, callback: (LiveData<User>) -> Unit) {
        loadingState.postValue(LoadingState.LOADING)
        try {
            var lastUpdated: Long = User.lastUpdated

            firebaseModel.getUserById(userId) { user ->
                executor.execute {

                    if (user != null) {
                        database.userDao().insertUser(user)

                        user.lastUpdated?.let {
                            if (lastUpdated < it) {
                                lastUpdated = it
                            }
                        }
                    }

                    val user = database.userDao().getUserById(userId)
                    mainHandler.post {
                        callback(user)
                    }
                    loadingState.postValue(LoadingState.LOADED)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users: ${e.message}")
        }

    }

    fun getNearbyUsers(currentLocation: GeoPoint, radiusInKm: Double, callback: (LiveData<List<User>>) -> Unit) {
        loadingState.postValue(LoadingState.LOADING)

//        firebaseModel.getNearbyUsers(
//            currentLocation = currentLocation, radiusInKm = radiusInKm, callback = callback
//        )
        executor.execute {
            try {
                var lastUpdated: Long = User.lastUpdated
                val geoHashBounds = GeoUtils.getGeoHashRange(currentLocation, radiusInKm)

                firebaseModel.getNearbyUsers(
                    currentLocation = currentLocation, radiusInKm = radiusInKm,
                    sinceLastUpdated = lastUpdated
                ) { usersList ->
                    val latestTime = lastUpdated

                    for (user in usersList) {
                        database.userDao().insertUser(user)
                        user.lastUpdated?.let {
                            if (latestTime < it) {
                                lastUpdated = it
                            }
                        }
                    }
                    User.lastUpdated = latestTime
                    val users = database.userDao().getNearbyUsers(geoHashBounds.first, geoHashBounds.second)
                    mainHandler.post {
                        callback(users)
                    }
                    loadingState.postValue(LoadingState.LOADED)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching users: ${e.message}")
            }
        }
    }

    fun refreshAllUsers(callback:  (LiveData<List<User>>) -> Unit) {
        loadingState.postValue(LoadingState.LOADING)
        executor.execute {
            try {
                var lastUpdated: Long = User.lastUpdated

                firebaseModel.getAllUsers(lastUpdated) { usersList ->
                    executor.execute {
                        val latestTime = lastUpdated

                        for (user in usersList) {
                            database.userDao().insertUser(user)
                            user.lastUpdated?.let {
                                if (latestTime < it) {
                                    lastUpdated = it
                                }
                            }
                        }

                        User.lastUpdated = latestTime
                        val users = database.userDao().getAllUsers()
                        mainHandler.post {
                            callback(users)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching users: ${e.message}")
            }
        }

        loadingState.postValue(LoadingState.LOADING)
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

    fun getPostById(postId: String, callback: (LiveData<Post>) -> Unit) {
        loadingState.postValue(LoadingState.LOADING)

        try {
            var lastUpdated: Long = Post.lastUpdated

            firebaseModel.getPostById(postId) { fetchedPost ->
                executor.execute {

                    if (fetchedPost != null) {
                        database.postDao().insertPost(fetchedPost)

                        fetchedPost.lastUpdated?.let {
                            if (lastUpdated < it) {
                                lastUpdated = it
                            }
                        }
                    }

                    val post = database.postDao().getPostById(postId)
                    mainHandler.post {
                        callback(post)
                    }
                    loadingState.postValue(LoadingState.LOADED)

                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users: ${e.message}")
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

//    fun refreshAllPosts(callback:  (LiveData<List<Post>>) -> Unit) {
//        loadingState.postValue(LoadingState.LOADING)
////        executor.execute {
//        try {
//            var lastUpdated: Long = Post.lastUpdated
////                val existingPosts = database.postDao().getAllPosts() // Fetch all existing posts
//
//            // **LOG: Check the current size of Room before fetching new posts**
//            val currentRoomPosts = database.postDao().getAllPosts().value?.size ?: 0
//            Log.d(TAG, "Before fetching: Room contains $currentRoomPosts posts")
//
//            firebaseModel.getAllPosts(lastUpdated) { postsList ->
//                Log.d(TAG, "firebase fetching: firebase contains ${postsList.size  ?: 0} posts")
//
//                executor.execute {
//                    val latestTime = lastUpdated
////                        val mergedPosts = existingPosts.toMutableList() // Merge old + new posts
//
//                    for (post in postsList) {
//                        database.postDao().insertPost(post)
//                        post.lastUpdated?.let {
//                            if (latestTime < it) {
//                                lastUpdated = it
//                            }
//                        }
//                    }
//                    Post.lastUpdated = latestTime
//                    val posts = database.postDao().getAllPosts()
//                    Log.d(TAG, "After fetching: Room contains ${posts.value?.size ?: 0} posts")
//
//                    mainHandler.post {
//                        callback(posts)
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error fetching posts: ${e.message}")
//        }
////        }
//
//        loadingState.postValue(LoadingState.LOADING)
//    }

    // this was the last good one
    fun refreshAllPosts(callback:  (LiveData<List<Post>>) -> Unit) {
        loadingState.postValue(LoadingState.LOADING)
        var lastUpdated: Long = Post.lastUpdated

        firebaseModel.getAllPosts(lastUpdated) { posts ->
            executor.execute {
                val latestTime = lastUpdated

                for (post in posts) {
                    var userExists = database.userDao().getUserById(post.ownerId) != null

                    if (!userExists) {
                        Log.w(TAG, "User ${post.ownerId} not found in Room, fetching from Firebase...")

                        shared.getUserById(post.ownerId) { fetchedUser ->
                            if (fetchedUser != null) {
                                Log.d(TAG, "User ${post.ownerId} fetched from Firebase, inserting into Room")
                                try {
                                    database.postDao().insertPost(post)
                                    post.lastUpdated?.let {
                                        if (latestTime < it) {
                                            lastUpdated = it
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to insert post: ${post.id}", e)
                                }

                            } else {
                                Log.e(TAG, "User ${post.ownerId} not found in Firebase. Skipping post ${post.id}")
                            }
                        }
                    } else {
                        try {
                            database.postDao().insertPost(post)
                            post.lastUpdated?.let {
                                if (latestTime < it) {
                                    lastUpdated = it
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to insert post: ${post.id}", e)
                        }
                    }
                }

                Post.lastUpdated = lastUpdated
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