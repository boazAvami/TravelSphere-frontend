package com.syb.travelsphere.pages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User


class NearbyUsersViewModel : ViewModel() {
    private val locationAndRadiusLiveData = MutableLiveData<Pair<GeoPoint, Double>>()
    private val _nearbyUsers = MediatorLiveData<List<User>>()
    val nearbyUsers: LiveData<List<User>> = _nearbyUsers

    init {
        _nearbyUsers.addSource(locationAndRadiusLiveData) { (location, radius) ->
            val liveData = Model.shared.getNearbyUsers(location, radius)
            _nearbyUsers.addSource(liveData) { users ->
                _nearbyUsers.value = users
            }
        }
    }

    fun updateLocationAndRadius(newLocation: GeoPoint, newRadius: Double) {
        locationAndRadiusLiveData.value = Pair(newLocation, newRadius)
    }

    fun refreshNearbyUsers(currentLocation: GeoPoint, radiusInKm: Double) {
        Model.shared.refreshAllNearbyUsers(currentLocation, radiusInKm)
    }
}
