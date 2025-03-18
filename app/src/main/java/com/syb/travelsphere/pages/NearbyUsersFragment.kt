package com.syb.travelsphere.pages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.syb.travelsphere.components.UserListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.databinding.FragmentNearbyUsersBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.utils.GeoUtils

class NearbyUsersFragment : Fragment() {

    private var binding: FragmentNearbyUsersBinding? = null
    private val viewModel: NearbyUsersViewModel by viewModels() // ViewModel instance

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var usersListAdapter: UserListAdapter


    private var currentRadius = 30.0 // Default radius 30km
    private lateinit var loc: GeoPoint

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNearbyUsersBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding?.userListRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        refreshNearbyUsers()
        setupRecyclerView()

        // Set up radius buttons
        binding?.radius30kButton?.setOnClickListener {
            setRadius(30.0)
        }
        binding?.radius10kButton?.setOnClickListener {
            setRadius(10.0) }
        binding?.radius5kButton?.setOnClickListener {
            setRadius(5.0) }


        GeoUtils.getCurrentLocation(requireContext()) { userLocation ->
            if (userLocation != null) {
                this.loc = userLocation
            }
        }

        viewModel.nearbyUsers.observe(viewLifecycleOwner) { users ->
            Log.d(TAG, "UI updated: Received ${users?.size ?: 0} users for radius $currentRadius KM")

            binding?.userListRecyclerView?.post {
                Log.d(TAG, "onCreateView: users : ${users?.size}")
                usersListAdapter.update(users) // Ensure UI updates on main thread
            }

            binding?.mapComponent?.displayUsers(users) { userId, username ->
                val action = NearbyUsersFragmentDirections.actionGlobalDisplayUserFragment( userId,  username )
                findNavController().navigate(action)

            }
            usersListAdapter?.notifyDataSetChanged()
        }

        binding?.swipeToRefresh?.setOnRefreshListener {
            refreshNearbyUsers()
        }

        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        // Get current location immediately
        GeoUtils.getCurrentLocation(requireContext()) { userLocation ->
            if (userLocation != null) {
                Log.d(TAG, "Initial Location: Lat=${userLocation.latitude}, Lon=${userLocation.longitude}")
                binding?.mapComponent?.centerMapOnLocation(userLocation.latitude, userLocation.longitude)
                viewModel.refreshNearbyUsers(userLocation, currentRadius)
            }
        }

        // Start observing location changes
        GeoUtils.observeLocationChanges(requireContext()) { userLocation ->
            Log.d(TAG, "New Location: Lat=${userLocation.latitude}, Lon=${userLocation.longitude}")
            binding?.mapComponent?.centerMapOnLocation(userLocation.latitude, userLocation.longitude)
            viewModel.refreshNearbyUsers(userLocation, currentRadius)
        }
    }

    override fun onResume() {
        super.onResume()
        // Get current location immediately
        refreshNearbyUsers() // Only refresh without re-observing location updates

        // Start observing location changes
        GeoUtils.observeLocationChanges(requireContext()) { userLocation ->
            Log.d(TAG, "New Location: Lat=${userLocation.latitude}, Lon=${userLocation.longitude}")
            binding?.mapComponent?.centerMapOnLocation(userLocation.latitude, userLocation.longitude)
            viewModel.refreshNearbyUsers(userLocation, currentRadius)
        }

        refreshNearbyUsers()
    }

    private fun setupRecyclerView() {
        // Adding divider between rows
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding?.userListRecyclerView?.addItemDecoration(dividerItemDecoration)

        binding?.userListRecyclerView?.setHasFixedSize(true)
        binding?.userListRecyclerView?.layoutManager = LinearLayoutManager(context)
        usersListAdapter = UserListAdapter(emptyList()) { user ->
            user.phoneNumber?.let { showUserPhonePopup(it) }
            user.location?.latitude?.let { lat ->
                user.location.longitude.let { lon ->
                    binding?.mapComponent?.centerMapOnLocation(lat, lon)
                }
            }
        }
        binding?.userListRecyclerView?.adapter = usersListAdapter
    }

    private fun setRadius(radius: Double) {
        if (currentRadius == radius) return // Prevent redundant updates

        currentRadius = radius
        usersListAdapter.update(emptyList()) // Clear UI before fetching new users

        Log.d(TAG, "Updating radius: $radius KM at location")
        refreshNearbyUsers()
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun refreshNearbyUsers() {
        GeoUtils.getCurrentLocation(requireContext()) { userLocation ->
            if (userLocation != null) {
                Log.d(TAG, "location: ${userLocation.latitude}, ${userLocation.longitude}")

                binding?.mapComponent?.centerMapOnLocation(userLocation.latitude, userLocation.longitude)
                viewModel.refreshNearbyUsers(userLocation, currentRadius)
            }

        }
    }

    companion object {
        private const val TAG = "NearbyUsersFragment"
    }
}
