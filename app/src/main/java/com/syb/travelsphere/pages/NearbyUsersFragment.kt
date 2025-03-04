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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.R
import com.syb.travelsphere.components.MapComponent
import com.syb.travelsphere.components.UserListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.databinding.FragmentAllPostsBinding
import com.syb.travelsphere.databinding.FragmentNearbyUsersBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NearbyUsersFragment : Fragment() {

    private var binding: FragmentNearbyUsersBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

//    private var users = listOf<User>()
    private var currentRadius = 30000.0 // Default radius 30km

    var nearbyUsers: LiveData<List<User>> = Model.shared.users // TODO : change to nearby

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNearbyUsersBinding.inflate(inflater, container, false)

        // for live data
        binding?.swipeToRefresh?.setOnRefreshListener {
            fetchNearbyUsers()
            //TODO get all near by users
        }

        // for loading animation
        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

//        binding?.userListRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
//
//        // Adding divider between rows
//        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
//        binding?.userListRecyclerView?.addItemDecoration(dividerItemDecoration)
//
//        // Set up radius buttons
//        binding?.radius30kButton?.setOnClickListener { setRadius(30000.0) }
//        binding?.radius10kButton?.setOnClickListener { setRadius(10000.0) }
//        binding?.radius5kButton?.setOnClickListener { setRadius(500.0) }
//
//        fetchNearbyUsersAndSetUpScreen()
        setupRecyclerView()
        setupRadiusButtons()
        fetchNearbyUsers() // Initial fetch
    }

    override fun onResume() {
        super.onResume()

        fetchNearbyUsers()
    }

//    private fun setRadius(radius: Double) {
//        currentRadius = radius
//        fetchNearbyUsersAndSetUpScreen() // Refresh with new radius
//    }

    private fun setupRecyclerView() {
        binding?.userListRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.userListRecyclerView?.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        )
    }

    private fun setupRadiusButtons() {
        binding?.radius30kButton?.setOnClickListener { setRadius(30000.0) }
        binding?.radius10kButton?.setOnClickListener { setRadius(10000.0) }
        binding?.radius5kButton?.setOnClickListener { setRadius(5000.0) }
    }

    private fun setRadius(radius: Double) {
        currentRadius = radius
        fetchNearbyUsers() // Refresh with new radius
    }
//
//    private fun fetchNearbyUsersAndSetUpScreen() {
//        lifecycleScope.launch {
//            try {
//                val fetchedUsers = withContext(Dispatchers.IO) {
//                    val userLocation = getCurrentUserLocation();
//                    binding?.mapComponent?.centerMapOnLocation(userLocation.latitude, userLocation.longitude)
//                    travelService.getNearbyUsers(userLocation.longitude, userLocation.latitude, currentRadius)
//                }
//
//                if (fetchedUsers != null) {
//                    users = fetchedUsers
//                    binding?.userListRecyclerView?.adapter = UserListAdapter(users) { user ->
//                        showUserEmailPopup(user.email)
//                        binding?.mapComponent?.centerMapOnLocation(user.location.coordinates[1], user.location.coordinates[0])
//                    }
//
//                    binding?.mapComponent?.displayUsers(users) // Assuming we can convert users to posts for display
//                }
//
//            } catch (e: Exception) {
//                if (!isAdded || context == null) {
//                    return@launch // Exit early if the fragment is not attached
//                }
//                Toast.makeText(requireContext(), "Error fetching nearby users: ${e.message}", Toast.LENGTH_SHORT).show()
//                Log.d("Error", "Error fetching nearby users: ${e.message}")
//            }
//        }
//    }

    private fun fetchNearbyUsers() {
        getCurrentUserLocation { userLocation ->
            binding?.mapComponent?.centerMapOnLocation(userLocation.latitude, userLocation.longitude)

            Model.shared.getNearbyUsers(userLocation, currentRadius).observe(viewLifecycleOwner, Observer { users ->
                if (users.isNotEmpty()) {
                    binding?.userListRecyclerView?.adapter = UserListAdapter(users) { user ->
                        showUserEmailPopup(user.phoneNumber ?: "No phone number available")
                        user.location?.let {
                            binding?.mapComponent?.centerMapOnLocation(it.latitude, it.longitude)
                        }
                    }
                    binding?.mapComponent?.displayUsers(users)
                } else {
                    Toast.makeText(requireContext(), "No nearby users found", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Function to show the user's phone in a popup
    private fun showUserEmailPopup(phone: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("User Content")
            .setMessage("phone: $phone")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
//
//    private fun getCurrentUserLocation(): GeoPoint {
//        var userLocation = GeoPoint(31.771959, 34.651401) // Default location (Jerusalem, Israel)
//
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(
//                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Request permissions if not granted
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
//                1
//            )
//
//            return userLocation // Returning default location
//        }
//
//        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//            if (location != null) {
//                // Success: Get the current location
//                Log.d("UserLocation", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
//                userLocation = GeoPoint(location.latitude, location.longitude)
//            } else {
//                // Location is null, handle this case (e.g., use default location)
//                Log.d("UserLocation", "Location is null, using default: $userLocation")
//            }
//        }
//
//        return userLocation // Return updated location once fetched
//    }

    private fun getCurrentUserLocation(callback: (GeoPoint) -> Unit) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("UserLocation", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                callback(GeoPoint(location.latitude, location.longitude))
            } else {
                Log.d("UserLocation", "Location is null, using default")
                callback(GeoPoint(31.771959, 34.651401)) // Default: Jerusalem
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
