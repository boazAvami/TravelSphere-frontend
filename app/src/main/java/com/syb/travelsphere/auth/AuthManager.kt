package com.syb.travelsphere.auth

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.syb.travelsphere.base.AuthCallback
import com.syb.travelsphere.base.EmptyCallback
import com.syb.travelsphere.base.MyApplication.Globals.context
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User

class AuthManager {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signUpUser(email: String, password: String, username: String, phone: String, isLocationShared: Boolean, profilePicture: Bitmap?, callback: AuthCallback) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                firebaseUser?.let {
                    val user = User(
                        id = it.uid,
                        userName = username,
                        phoneNumber = phone,
                        profilePictureUrl = "",
                        isLocationShared = isLocationShared
                    )

                    Model.shared.addUser(user, profilePicture) {
                        Log.d(TAG, "User created & added to Firestore successfully!")

                        // Fetch the user (this ensures it's also inserted into Room)
                        Model.shared.getUserById(it.uid) { fetchedUser ->
                            callback(firebaseUser) // Callback after data is in Room
                        }

                        callback(firebaseUser)
                    }
                } ?: run {
                    Log.e(TAG, "Firebase user is null after sign-up.")
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                handleAuthFailure(exception, "Failed to create user")
                callback(null)
            }
    }

    fun signInUser(email: String, password: String, callback: AuthCallback) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "User signed in successfully!")

                auth.currentUser?.let { user ->
                    // Fetch the user (which inserts into Room)
                    Model.shared.getUserById(user.uid) { fetchedUser ->
                        callback(auth.currentUser)
                    }
                } ?: callback(null)
            }
            .addOnFailureListener { exception ->
                handleAuthFailure(exception, "⚠️ Sign-in failed")
                callback(null)
            }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signOut(callback: EmptyCallback) {
        auth.signOut()
        Log.d(TAG, "✅ User signed out successfully.")
        callback()
    }

    fun updateUserPassword(newPassword: String, callback: EmptyCallback) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "⚠️ No authenticated user found.")
            return
        }

        currentUser.updatePassword(newPassword)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Password updated successfully.")
                callback()
            }
            .addOnFailureListener { exception ->
                handleAuthFailure(exception, "⚠️ Failed to update password")
            }
    }

    // Centralized Error Handling Function
    private fun handleAuthFailure(exception: Exception, message: String) {
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                Log.w(TAG, "$message: Weak password (${exception.message})")
                Toast.makeText(
                    context,
                    "⚠️ Password must be at least 6 characters long",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is FirebaseAuthInvalidCredentialsException -> {
                Log.w(TAG, "$message: Invalid credentials (${exception.message})")
                Toast.makeText(context, "⚠️ Invalid email or password", Toast.LENGTH_SHORT).show()
            }

            is FirebaseAuthUserCollisionException -> {
                Log.w(TAG, "$message: Email already in use (${exception.message})")
                Toast.makeText(
                    context,
                    "⚠️ Email is already associated with another account",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is FirebaseAuthRecentLoginRequiredException -> {
                Log.w(TAG, "$message: Recent login required (${exception.message})")
                Toast.makeText(
                    context,
                    "⚠️ Please log in again to perform this action",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                Log.e(TAG, "$message: ${exception.message}")
                Toast.makeText(
                    context,
                    "⚠️ An error occurred. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val TAG = "FirebaseAuthManager"
    }
}
