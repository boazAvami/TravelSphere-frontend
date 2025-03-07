package com.syb.travelsphere.pages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User

class NearbyUsersViewModel : ViewModel() {

    val nearbyUsers: LiveData<List<User>> = Model.shared.users
//    val nearbyUsers: LiveData<List<User>> = Model.shared.nearbyUsers

    fun refreshNearbyUsers(currentLocation: GeoPoint, radiusInKm: Double) {
        //Model.shared.updateGeoHashBounds(currentLocation, radiusInKm) // ✅ Update geohash bounds
        //Model.shared.refreshNearbyUsers(currentLocation, radiusInKm) // ✅ Updates Room
        Model.shared.refreshAllUsers()
    }
}
