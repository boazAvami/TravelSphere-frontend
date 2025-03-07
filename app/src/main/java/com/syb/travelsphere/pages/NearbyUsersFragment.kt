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
import androidx.fragment.app.viewModels
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
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.databinding.FragmentNearbyUsersBinding
import com.syb.travelsphere.model.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NearbyUsersFragment : Fragment() {

    private var binding: FragmentNearbyUsersBinding? = null
    private val viewModel: NearbyUsersViewModel by viewModels() // ViewModel instance

    private lateinit var userListRecyclerView: RecyclerView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentRadius = 30000.0 // Default radius 30km

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNearbyUsersBinding.inflate(inflater, container, false)

        viewModel.nearbyUsers.observe(viewLifecycleOwner) { users ->
            userListRecyclerView.adapter = UserListAdapter(users) { user ->
                user.phoneNumber?.let { showUserPhonePopup(it) }
                user.location?.latitude?.let { lat ->
                    user.location.longitude.let { lon ->
                        binding?.mapComponent?.centerMapOnLocation(lat, lon)
                    }
                }
            }

            binding?.mapComponent?.displayUsers(users)
//            userListRecyclerView.adapter.notifyDataSetChanged()
        }

        binding?.swipeToRefresh?.setOnRefreshListener {
//            fetchNearbyUsersAndSetUpScreen()
            val userLocation = getCurrentUserLocation() { userLocation ->
                binding?.mapComponent?.centerMapOnLocation(userLocation.latitude, userLocation.longitude)
                viewModel.refreshNearbyUsers(userLocation, currentRadius)
            }
        }

        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        userListRecyclerView = view.findViewById(R.id.userListRecyclerView)
        userListRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Adding divider between rows
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        userListRecyclerView.addItemDecoration(dividerItemDecoration)

        // Set up radius buttons
        binding?.radius30kButton?.setOnClickListener {
            setRadius(30000.0)
        }
        binding?.radius10kButton?.setOnClickListener {
            setRadius(10000.0) }
        binding?.radius5kButton?.setOnClickListener {
            setRadius(500.0) }

        fetchNearbyUsersAndSetUpScreen()
    }

    private fun setRadius(radius: Double) {
        currentRadius = radius
        fetchNearbyUsersAndSetUpScreen() // Refresh with new radius
    }

    private fun fetchNearbyUsersAndSetUpScreen() {
            try {
                getCurrentUserLocation { userLocation ->
                    binding?.mapComponent?.centerMapOnLocation(userLocation.latitude, userLocation.longitude)
                    viewModel.refreshNearbyUsers(userLocation, currentRadius)
                }
                userListRecyclerView.adapter = UserListAdapter(viewModel.nearbyUsers.value) { user ->
                    user.phoneNumber?.let { showUserPhonePopup(it) }
                    user.location?.latitude?.let { lat ->
                        user.location.longitude.let { lon ->
                            binding?.mapComponent?.centerMapOnLocation(lat,
                                lon
                            )
                        }
                    }
                }

                binding?.mapComponent?.displayUsers(viewModel.nearbyUsers.value) // Assuming we can convert users to posts for display

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching nearby users: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("Error", "Error fetching nearby users: ${e.message}")
            }
    }

    // Function to show the user's phone number in a popup
    private fun showUserPhonePopup(phone: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("User Phone number")
            .setMessage("Phone number: $phone")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun getCurrentUserLocation(callback: (GeoPoint) -> Unit) {
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
            callback(GeoPoint(31.771959, 34.651401)) // Return default location if no permission
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLocation = GeoPoint(location.latitude, location.longitude)
                Log.d("UserLocation", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                callback(userLocation) // ✅ Return actual location via callback
            } else {
                Log.d("UserLocation", "Location is null, using default")
                callback(GeoPoint(31.771959, 34.651401)) // Return default location if null
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        private const val TAG = "AllPostsFragment"
    }
}