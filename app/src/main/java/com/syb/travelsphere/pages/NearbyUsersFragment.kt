package com.syb.travelsphere.pages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.R
import com.syb.travelsphere.components.MapComponent
import com.syb.travelsphere.components.UserListAdapter
import com.syb.travelsphere.services.TravelService
import com.syb.travelsphere.services.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint

class NearbyUsersFragment : Fragment() {

    private lateinit var travelService: TravelService
    private lateinit var userListRecyclerView: RecyclerView
    private lateinit var mapComponent: MapComponent
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var users = listOf<User>()
    private var currentRadius = 30000.0 // Default radius 30km

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nearby_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        travelService = TravelService()

        userListRecyclerView = view.findViewById(R.id.userListRecyclerView)
        userListRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Adding divider between rows
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        userListRecyclerView.addItemDecoration(dividerItemDecoration)

        mapComponent = view.findViewById(R.id.mapComponent)

        // Set up radius buttons
        view.findViewById<Button>(R.id.radius30k).setOnClickListener { setRadius(30000.0) }
        view.findViewById<Button>(R.id.radius10k).setOnClickListener { setRadius(10000.0) }
        view.findViewById<Button>(R.id.radius5k).setOnClickListener { setRadius(500.0) }

        fetchNearbyUsersAndSetUpScreen()
    }

    private fun setRadius(radius: Double) {
        currentRadius = radius
        fetchNearbyUsersAndSetUpScreen() // Refresh with new radius
    }

    private fun fetchNearbyUsersAndSetUpScreen() {
        lifecycleScope.launch {
            try {
                val fetchedUsers = withContext(Dispatchers.IO) {
                    val userLocation = getCurrentUserLocation();
                    mapComponent.centerMapOnLocation(userLocation.latitude, userLocation.longitude)
                    travelService.getNearbyUsers(userLocation.longitude, userLocation.latitude, currentRadius)
                }

                if (fetchedUsers != null) {
                    users = fetchedUsers
                    userListRecyclerView.adapter = UserListAdapter(users) { user ->
                        showUserEmailPopup(user.email)
                        mapComponent.centerMapOnLocation(user.location.coordinates[1], user.location.coordinates[0])
                    }

                    mapComponent.displayUsers(users) // Assuming we can convert users to posts for display
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching nearby users: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("Error", "Error fetching nearby users: ${e.message}")
            }
        }
    }

    // Function to show the user's email in a popup
    private fun showUserEmailPopup(email: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("User Email")
            .setMessage("Email: $email")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun getCurrentUserLocation(): GeoPoint {
        var userLocation = GeoPoint(31.771959, 34.651401) // Default location (Jerusalem, Israel)

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )

            return userLocation // Returning default location
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Success: Get the current location
                Log.d("UserLocation", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                userLocation = GeoPoint(location.latitude, location.longitude)
            } else {
                // Location is null, handle this case (e.g., use default location)
                Log.d("UserLocation", "Location is null, using default: $userLocation")
            }
        }

        return userLocation // Return updated location once fetched
    }
}
