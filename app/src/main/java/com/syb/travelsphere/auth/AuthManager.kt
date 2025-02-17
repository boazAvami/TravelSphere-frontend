package com.syb.travelsphere.auth

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
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
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        val user = User(
                            id = it.uid,
                            email = email,
                            userName = username,
                            phoneNumber = phone,
                            profilePictureUrl = "",
//                            password = password,
                            isLocationShared = isLocationShared
                        )

                        Model.shared.addUser(user, profilePicture) {
                            Log.d(TAG, "createUserWithEmail:success")
                            callback(firebaseUser)
                        }
                    }
                } else {
                    // 🔹 Handle Weak Password Error
                    if (task.exception is FirebaseAuthWeakPasswordException) {
                        Log.w(TAG, "Weak password: ${task.exception?.message}")
                        Toast.makeText(context, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    }
                    callback(null)
                }
            }.addOnSuccessListener {  }
    }

    fun signInUser(email: String, password: String, callback: AuthCallback) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    callback(auth.currentUser)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    callback(null)
                }
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
        callback()
    }

    fun updateUserPassword(newPassword: String, callback: EmptyCallback) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found.")
            return
        }

        currentUser.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Password updated successfully.")
                    callback()
                } else {
                    Log.e(TAG, "Failed to update password: ${task.exception?.message}")
                }
            }
    }


    companion object {
        private const val TAG = "FirebaseAuthManager"
    }
}
