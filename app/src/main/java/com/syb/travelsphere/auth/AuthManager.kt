package com.syb.travelsphere.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.syb.travelsphere.base.AuthCallback
import com.syb.travelsphere.base.EmptyCallback

class AuthManager {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signUpUser(email: String, password: String, callback: AuthCallback) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    callback(auth.currentUser)
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    callback(null)
                }
            }
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

    companion object {
        private const val TAG = "FirebaseAuthManager"
    }
}
