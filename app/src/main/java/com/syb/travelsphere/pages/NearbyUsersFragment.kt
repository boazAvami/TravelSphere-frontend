package com.syb.travelsphere.pages

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
import com.google.firebase.firestore.GeoPoint
import com.google.android.gms.location.LocationServices
import com.syb.travelsphere.components.UserListAdapter
import com.syb.travelsphere.databinding.FragmentNearbyUsersBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.utils.GeoUtils

class NearbyUsersFragment : Fragment() {

    private var binding: FragmentNearbyUsersBinding? = null
    private val viewModel: NearbyUsersViewModel by viewModels()
    private lateinit var usersListAdapter: UserListAdapter
    private var currentRadius = 30.0 // Default radius 30km

    companion object {
        private const val TAG = "NearbyUsersFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNearbyUsersBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupRadiusButtons()
        setupObservers()

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        refreshNearbyUsers()
    }

    private fun setupRecyclerView() {
        binding?.userListRecyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)

            // Adding divider between rows
            val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)

            // Initialize adapter with empty data
            usersListAdapter = UserListAdapter(emptyList()) { user ->
                user.phoneNumber?.let { showUserPhonePopup(it) }
                user.location?.let { location ->
                    binding?.mapComponent?.centerMapOnLocation(location.latitude, location.longitude)
                }
            }

            adapter = usersListAdapter
        }
    }

    private fun setupRadiusButtons() {
        binding?.radius30kButton?.setOnClickListener { setRadius(30.0) }
        binding?.radius10kButton?.setOnClickListener { setRadius(10.0) }
        binding?.radius5kButton?.setOnClickListener { setRadius(5.0) }
    }

    private fun setupObservers() {
        // Observe nearby users
        viewModel.nearbyUsers.observe(viewLifecycleOwner) { users ->
            Log.d(TAG, "UI updated: Received ${users?.size ?: 0} users for radius $currentRadius KM")
            usersListAdapter.update(users)

            binding?.mapComponent?.displayUsers(users) { userId, username ->
                val action = NearbyUsersFragmentDirections.actionGlobalDisplayUserFragment(userId, username)
                findNavController().navigate(action)
            }
        }

        // Observe loading state
        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
        }

        // Set up swipe to refresh
        binding?.swipeToRefresh?.setOnRefreshListener {
            refreshNearbyUsers()
        }
    }

    private fun setRadius(radius: Double) {
        if (currentRadius == radius) return // Prevent redundant updates

        currentRadius = radius
        usersListAdapter.update(emptyList()) // Clear UI before fetching new users

        Log.d(TAG, "Updating radius: $radius KM at location")
        refreshNearbyUsers()
    }

    private fun showUserPhonePopup(phone: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("User Phone number")
            .setMessage("Phone number: $phone")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}