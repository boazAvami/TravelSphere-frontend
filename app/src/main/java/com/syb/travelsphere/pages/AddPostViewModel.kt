package com.syb.travelsphere.pages

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.networking.LocationsService

class AddPostViewModel : ViewModel() {

    private val authManager = AuthManager()
    private val locationService = LocationsService()

    private val _selectedImages = MutableLiveData<MutableList<Bitmap>>(mutableListOf())
    val selectedImages: LiveData<MutableList<Bitmap>> = _selectedImages

    private val _currentGeoPoint = MutableLiveData<GeoPoint?>()
    val currentGeoPoint: LiveData<GeoPoint?> = _currentGeoPoint

    private val _locationSuggestions = MutableLiveData<List<String>>()
    val locationSuggestions: LiveData<List<String>> = _locationSuggestions

    private val _postCreated = MutableLiveData<Boolean>()
    val postCreated: LiveData<Boolean> = _postCreated

    fun addImage(bitmap: Bitmap) {
        _selectedImages.value?.let {
            if (!it.contains(bitmap)) {
                it.add(bitmap)
                _selectedImages.postValue(it)
            }
        }
    }

    fun removeImage(position: Int) {
        _selectedImages.value?.let {
            it.removeAt(position)
            _selectedImages.postValue(it)
        }
    }

    fun fetchAddressSuggestions(query: String) {
        locationService.fetchAddressSuggestions(query) { suggestions ->
            _locationSuggestions.postValue(suggestions ?: emptyList())
        }
    }

    fun fetchGeoLocation(address: String) {
        locationService.fetchGeoLocation(address) { geoPoint ->
            _currentGeoPoint.postValue(geoPoint)
        }
    }

    fun createPost(description: String, locationName: String) {
        authManager.getCurrentUser()?.let { user ->
            val timestamp = Timestamp.now()
            val post = Post(
                id = "",
                locationName = locationName,
                description = description,
                photos = listOf(),
                location = _currentGeoPoint.value ?: GeoPoint(0.0, 0.0),
                creationTime = timestamp,
                ownerId = user.uid
            )

            Model.shared.addPost(post, _selectedImages.value ?: emptyList()) {
                Log.d(TAG, "Post created with ${_selectedImages.value?.size} photos")
                _postCreated.postValue(true)
            }
        }
    }

    companion object {
        private const val TAG = "AddPostViewModel"
    }
}
