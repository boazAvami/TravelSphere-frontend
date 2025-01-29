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
        callback(listOf())
        database.collection(Constants.COLLECTIONS.USERS).get().addOnCompleteListener {
            when (it.isSuccessful){
                true -> { // Operation succeeded
                    val users: MutableList<User> = mutableListOf()
                    for (userJson in it.result) {
                        users.add(User.fromJson(userJson.data))
                    }
                }
                false -> callback(listOf()) // Operation failed
            }
        }
    }

    fun addUser(user: User, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.USERS).document(user.id.toString()).set(user.json)
            .addOnCompleteListener{
                callback() // Operation succeeded, execute the callback
            }
    }

    fun deleteUser(userId: String, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.USERS).document(userId).delete()
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
        database.collection(Constants.COLLECTIONS.USERS).document(user.id.toString()).set(user.json)
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