package com.syb.travelsphere.model

import android.util.Log
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.syb.travelsphere.base.Constants
import com.syb.travelsphere.base.EmptyCallback
import com.syb.travelsphere.base.UsersCallback

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
                            Log.d("TAG", "${document.id} => ${document.data}")
                        }
                        callback(users)
                    }
                    false -> callback(listOf())
                }
            }
            .addOnFailureListener {
                error -> Log.w("TAG", "Error getting document", error)
            }

    }

    fun getUserById(userId: String, callback: UsersCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("TAG","Gey document: ${document.id} successfully")
                    // TODO: add a callback
                } else {
                    Log.d("TAG", "No such document")
                }
            }
            .addOnFailureListener {
                Log.d("TAG", "Error Getting Document: $userId")

            }
    }

    fun addUser(user: User, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .document(user.id.toString())
            .set(user.json)
            .addOnCompleteListener{
                callback() // Operation succeeded, execute the callback
            }
            .addOnFailureListener { error -> Log.w("TAG", "Error writing document", error) }
    }

    fun deleteUser(userId: String, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .document(userId)
            .delete()
            .addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        callback() // Operation succeeded
                    }
                    false -> {
                        task.exception?.let { // Operation failed, log the error
                            Log.e("FirestoreError", "Error deleting user: ${it.message}")
                        }
                    }
                }
            }
    }

    fun editUser(user: User, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .document(user.id.toString())
            .set(user.json) // overwrite the entire document
            .addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        callback() // Operation succeeded
                    }
                    false -> {
                        task.exception?.let {  // Operation failed, log the error
                            Log.e("FirestoreError", "Error editing user: ${it.message}")
                        }
                    }
                }
            }
    }
}