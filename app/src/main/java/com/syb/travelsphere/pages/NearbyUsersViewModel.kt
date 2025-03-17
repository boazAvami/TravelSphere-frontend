package com.syb.travelsphere.pages

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.model.Model


class NearbyUsersViewModel : ViewModel() {
    val nearbyUsers = Model.shared.nearbyUsers

    fun refreshNearbyUsers(currentLocation: GeoPoint, radiusInKm: Double) {
        Log.d("NearbyUsersViewModel", "Refreshing users for radius: $radiusInKm KM")

        Model.shared.refreshAllNearbyUsers(currentLocation, radiusInKm) {
                val users = Model.shared.getNearbyUsers(currentLocation, radiusInKm)
                Log.d("NearbyUsersViewModel", "Good Updated LiveData with ${users.value?.size ?: 0} users")
        }
    }

}
