package com.syb.travelsphere.model

import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
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

class FirebaseModel {
    private val database = Firebase.firestore

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }
        database.firestoreSettings = settings
    }

    fun getAllUsers(callback: UsersCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val users: MutableList<User> = mutableListOf()
                        for (document in it.result) {
                            users.add(User.fromJSON(document.data))
                            Log.d(TAG, "${document.id} => ${document.data}")
                        }
                        callback(users)
                    }
                    false -> callback(listOf())
                }
            }
            .addOnFailureListener {
                error -> Log.w(TAG, "Error getting document", error)
            }
    }

    fun getUserById(userId: String, callback: UserCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .document(userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.data?.let { User.fromJSON(it) }
                    Log.d(TAG,"Get document: ${task.result?.id} successfully")
                    callback(user)
                } else {
                    Log.d(TAG, "Error fetching user: ${task.exception?.message}")
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error Getting Document: $userId")

            }
    }

    fun getNearbyUsers(currentLocation: GeoPoint, radiusInKm: Double, callback: UsersCallback) {
        val center = GeoLocation(currentLocation.latitude, currentLocation.longitude)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInKm * 1000) // Convert km to meters

        val usersList = mutableListOf<User>()
        val tasks = bounds.map { bound ->
            database.collection(Constants.COLLECTIONS.USERS)
                .whereGreaterThanOrEqualTo(User.GEOHASH_KEY, bound.startHash)
                .whereLessThanOrEqualTo(User.GEOHASH_KEY, bound.endHash)
                .get()
        }

        val allTasks = com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
        allTasks.addOnSuccessListener { results ->
            results.forEach { task ->
                if (task.isSuccessful) {
                    val documents = (task.result as? com.google.firebase.firestore.QuerySnapshot)?.documents
                    documents?.forEach { doc ->
                        val user = User.fromJSON(doc.data ?: emptyMap())

                        // 🔹 Filter based on actual radius
                        if (GeoUtils.isWithinRadius(currentLocation, user.location ?: GeoPoint(0.0, 0.0), radiusInKm)) {
                            usersList.add(user)
                        }
                    }
                }
            }
            callback(usersList)
        }.addOnFailureListener { callback(emptyList()) }
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

    fun getAllPosts(callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()
                        for (document in it.result) {
                            posts.add(Post.fromJSON(document.data))
                            Log.d(TAG, "${document.id} => ${document.data}")
                        }
                        callback(posts)
                    }
                    false -> callback(listOf())
                }
            }
            .addOnFailureListener {
                    error -> Log.w(TAG, "Error getting document", error)
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

    fun addPost(post: Post, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .document(post.id)
            .set(post.json)
            .addOnCompleteListener{
                callback() // Operation succeeded, execute the callback
            }
            .addOnFailureListener { error -> Log.w(TAG, "Error writing document", error) }
    }

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

    companion object {
        private const val TAG = "FirestoreModel"
    }
}