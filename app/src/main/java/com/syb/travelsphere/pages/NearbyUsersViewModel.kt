package com.syb.travelsphere.pages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User

class NearbyUsersViewModel : ViewModel() {

//    val nearbyUsers: LiveData<List<User>> = Model.shared.nearbyUsers
//    val currentLocation: LiveData<GeoPoint> = Model.shared.currentLocation
//    val radius: LiveData<Double> = Model.shared.radius

    val nearbyUsers: LiveData<List<User>?> = Model.shared.nearbyUsers
//    val nearbyUsers: LiveData<List<User>> = Model.shared.nearbyUsers

//    fun refreshNearbyUsers(currentLocation: GeoPoint, radiusInKm: Double) {
//        Model.shared.refreshAllUsers()
//    }

//    fun refreshNearbyUsers(currentLocation: GeoPoint, radiusInKm: Double) {
//        Model.shared.refreshNearbyUsers(currentLocation, radiusInKm)
//    }
    fun refreshNearbyUsers(currentLocation: GeoPoint, radiusInKm: Double) {
        Model.shared.refreshNearbyUsers(currentLocation, radiusInKm)
    }
}
