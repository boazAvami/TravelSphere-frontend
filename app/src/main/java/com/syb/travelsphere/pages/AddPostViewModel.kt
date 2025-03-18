package com.syb.travelsphere.pages

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post

class AddPostViewModel : ViewModel() {

    private val authManager = AuthManager()

    val selectedImages = mutableListOf<Bitmap>()

    val locationSuggestions = Model.shared.addressSuggestions
    val currentGeoPoint = Model.shared.geoLocation

    fun addImage(bitmap: Bitmap) {
        if (!selectedImages.contains(bitmap)) {
            selectedImages.add(bitmap)
            // Return true if this function needs to indicate success
        }
    }

    fun removeImage(position: Int) {
        if (position >= 0 && position < selectedImages.size) {
            selectedImages.removeAt(position)
            // Return true if this function needs to indicate success
        }
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
                photos = listOf(),
                location = currentGeoPoint.value ?: GeoPoint(0.0, 0.0),
                creationTime = timestamp,
                ownerId = user.uid
            )

            Model.shared.addPost(post, selectedImages) {
                Log.d(TAG, "Post created with ${selectedImages.size} photos")
                onPostCreated()
            }
        }
    }

    companion object {
        private const val TAG = "AddPostViewModel"
    }
}