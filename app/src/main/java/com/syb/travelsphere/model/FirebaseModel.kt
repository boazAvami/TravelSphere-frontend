package com.syb.travelsphere.model

import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.syb.travelsphere.base.Constants
import com.syb.travelsphere.base.EmptyCallback
import com.syb.travelsphere.base.PostCallback
import com.syb.travelsphere.base.PostsCallback
import com.syb.travelsphere.base.UserCallback
import com.syb.travelsphere.base.UsersCallback
import com.syb.travelsphere.utils.GeoUtils
import com.syb.travelsphere.utils.TimeUtil.toFirebaseTimestamp
import java.util.Date

class FirebaseModel {
    private val database = Firebase.firestore

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }
        database.firestoreSettings = settings
    }

    fun getAllUsers(sinceLastUpdated: Long, callback: UsersCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .whereGreaterThanOrEqualTo(User.LAST_UPDATED_KEY, sinceLastUpdated.toFirebaseTimestamp)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val users: MutableList<User> = mutableListOf()
                        for (document in it.result) {
                            users.add(User.fromJSON(document.data))
                            Log.d(TAG, "Fetched post: ${document.id} => ${document.data}")
                        }
                        Log.d(TAG, "num of users: ${users.size}")
                        callback(users)
                    }
                    false -> callback(listOf())
                }
            }
    }

    fun getUserById(userId: String, callback: UserCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .document(userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.data?.let { User.fromJSON(it) }
                    Log.d(TAG,"Get user: ${task.result?.id} successfully")
                    callback(user)
                } else {
                    Log.d(TAG, "Error fetching user: ${task.exception?.message}")
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error Getting Document: $userId")

            }
    }

    private var lastQueriedLocation: GeoPoint? = null
    private var lastQueriedRadius: Double = -1.0

    fun getNearbyUsers(
        sinceLastUpdated: Long,
        currentLocation: GeoPoint,
        radiusInKm: Double,
        callback: UsersCallback
    ) {
        val (minGeoHash, maxGeoHash) = GeoUtils.getGeoHashRange(currentLocation, radiusInKm)
        val usersList = mutableListOf<User>()

        // 🔹 Detect if radius or location changed
        val locationChanged = lastQueriedLocation == null || lastQueriedLocation != currentLocation
        val radiusChanged = lastQueriedRadius == -1.0 || lastQueriedRadius != radiusInKm

        // 🔹 If radius or location changed, do a full refresh (ignore `sinceLastUpdated`)
        val query = database.collection(Constants.COLLECTIONS.USERS)
            .whereGreaterThanOrEqualTo(User.LAST_UPDATED_KEY, Timestamp(Date(sinceLastUpdated)))
            .whereGreaterThanOrEqualTo(User.GEOHASH_KEY, minGeoHash)
            .whereLessThanOrEqualTo(User.GEOHASH_KEY, maxGeoHash)

//        if (!locationChanged && !radiusChanged) {
//            query.whereGreaterThanOrEqualTo(User.LAST_UPDATED_KEY, Timestamp(Date(sinceLastUpdated)))
//        }

        query.get()
            .addOnSuccessListener { documents ->
                documents.documents.forEach { doc ->
                    val user = User.fromJSON(doc.data ?: emptyMap())

                    user.location?.let {
                        if (GeoUtils.isWithinRadius(currentLocation, it, radiusInKm)) {
                            usersList.add(user)
                        }
                    }
                }

                // ✅ Update last queried values
                lastQueriedLocation = currentLocation
                lastQueriedRadius = radiusInKm

                callback(usersList)
            }
            .addOnFailureListener { callback(emptyList()) }
    }



    fun addUser(user: User, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .document(user.id)
            .set(user.json)
            .addOnCompleteListener{
                callback() // Operation succeeded, execute the callback
            }
            .addOnFailureListener { error -> Log.w(TAG, "Error writing document", error) }
    }

    fun editUser(user: User, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .document(user.id)
            .set(user.json, SetOptions.merge()) // overwrite the document
            .addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        Log.d(TAG, "User ${user.id} updated successfully.")
                        callback() // Operation succeeded
                    }
                    false -> {
                        task.exception?.let {  // Operation failed, log the error
                            Log.e(TAG, "Error editing user: ${it.message}")
                        }
                    }
                }
            }
    }

    fun getAllPosts(sinceLastUpdated: Long, callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .whereGreaterThanOrEqualTo(User.LAST_UPDATED_KEY, sinceLastUpdated.toFirebaseTimestamp)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()
                        for (document in it.result) {
                            posts.add(Post.fromJSON(document.data))
                            Log.d(TAG, "Post received: ${document.id} => ${document.data}")
                        }
                        Log.d(TAG, "num of posts: ${posts.size}")
                        callback(posts)
                    }
                    false -> callback(listOf())
                }
            }
    }

    fun getPostById(postId: String, callback: PostCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .document(postId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val post = task.result?.data?.let { Post.fromJSON(it) }
                    Log.d(TAG,"Get post: ${task.result?.id} successfully")
                    callback(post)
                } else {
                    Log.d(TAG, "Error fetching post: ${task.exception?.message}")
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error Getting post: $postId")
            }
    }

    fun getPostsByUserId(userId: String, sinceLastUpdated: Long, callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .whereEqualTo(Post.OWNER_ID_KEY, userId)
            .whereGreaterThanOrEqualTo(User.LAST_UPDATED_KEY, Timestamp(Date(sinceLastUpdated)))
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()
                        for (document in it.result) {
                            posts.add(Post.fromJSON(document.data))
                            Log.d(TAG, "Post received: ${document.id} => ${document.data}")
                        }
                        callback(posts)
                    }
                    false -> callback(listOf())
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error fetching posts for user: $userId, ${exception.message}")
                callback(emptyList()) // Return empty list on failure
            }
    }

    fun addPost(post: Post, postRef: DocumentReference, callback: EmptyCallback) {
        val postWithId = post.copy(id = postRef.id)

        postRef.set(postWithId.json)
            .addOnCompleteListener {
                callback() // Operation succeeded, execute the callback
            }
            .addOnFailureListener { error -> Log.w(TAG, "Error writing document", error) }
    }

//    fun addPost(post: Post, callback: EmptyCallback) {
//        database.collection(Constants.COLLECTIONS.POSTS)
//            .document(post.id)
//            .set(post.json)
//            .addOnCompleteListener {
//                callback() // Operation succeeded, execute the callback
//            }
//            .addOnFailureListener { error -> Log.w(TAG, "Error writing document", error) }
//    }

    fun deletePost(postId: String, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .document(postId)
            .delete()
            .addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        callback() // Operation succeeded
                    }
                    false -> {
                        task.exception?.let { // Operation failed, log the error
                            Log.e(TAG, "Error deleting user: ${it.message}")
                        }
                    }
                }
            }
    }

    fun editPost(post: Post, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .document(post.id.toString())
            .set(post.json) // overwrite the entire document
            .addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        callback() // Operation succeeded
                    }
                    false -> {
                        task.exception?.let {  // Operation failed, log the error
                            Log.e(TAG, "Error editing user: ${it.message}")
                        }
                    }
                }
            }
    }

    fun generatePostReference(): DocumentReference {
        return database.collection(Constants.COLLECTIONS.POSTS).document() // ✅ Generate Firestore ID
    }

    companion object {
        private const val TAG = "FirestoreModel"
    }
}