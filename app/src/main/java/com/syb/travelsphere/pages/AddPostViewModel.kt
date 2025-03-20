package com.syb.travelsphere.pages

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post

class AddPostViewModel : ViewModel() {

    private val authManager = AuthManager()

    var selectedImage: Bitmap? = null
        private set

    val locationSuggestions = Model.shared.addressSuggestions
    val currentGeoPoint = Model.shared.geoLocation

    fun setImage(bitmap: Bitmap) {
        selectedImage = bitmap
    }

    fun clearImage() {
        selectedImage = null
    }

    fun fetchAddressSuggestions(query: String) {
        Model.shared.fetchAddressSuggestions(query)
    }

    fun fetchGeoLocation(address: String) {
        Model.shared.fetchGeoLocation(address)
    }

    fun createPost(description: String, locationName: String, onPostCreated: () -> Unit) {
        authManager.getCurrentUser()?.let { user ->
            val timestamp = Timestamp.now()
            val post = Post(
                id = "",
                locationName = locationName,
                description = description,
                photo = "",
                location = currentGeoPoint.value ?: GeoPoint(0.0, 0.0),
                creationTime = timestamp,
                ownerId = user.uid
            )

            Log.d("Addpost", "createPost: $post")

            Model.shared.addPost(post, selectedImage) {
                Log.d("Addpost", "createPost: 55")
                onPostCreated()
            }
        }
    }
}