package com.syb.travelsphere

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.syb.travelsphere.auth.AuthActivity
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.databinding.ActivityMainBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.utils.GeoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var navController: NavController? = null
    private lateinit var authManager: AuthManager

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authManager = AuthManager()

        if (!authManager.isUserLoggedIn()) {
            // Redirect to AuthActivity
            navigateToAuthActivity()
            return
        }

        if (checkLocationPermissions()) {
            updateUserLocationIfShared()
        } else {
            requestLocationPermissions()
        }

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Set up the Toolbar (ActionBar)
        setSupportActionBar(binding.mainToolbar)


        val navHostController: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as? NavHostFragment
        navController = navHostController?.navController

        // Set up the bottom navigation with the NavController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.allPostsFragment,
                R.id.nearbyUsersFragment,
                R.id.addPostFragment,
                R.id.profileFragment
            )
        )

        navController?.let {
            setupActionBarWithNavController(this, it, appBarConfiguration)
            NavigationUI.setupWithNavController(binding.mainBottomNavigationBar, it)
        }

        // Handle bottom navigation item reselection to avoid recreating fragments
        binding.mainBottomNavigationBar.setOnItemReselectedListener { item ->
            val currentFragment = navController?.currentDestination?.id
            if (currentFragment != item.itemId) {
                navController?.navigate(item.itemId)
            }
        }

        // Dynamically show/hide settings menu based on current fragment
        navController?.addOnDestinationChangedListener { _, destination, _ ->
            invalidateOptionsMenu() // Refresh the menu when navigation changes
        }

        checkAndRequestLocationPermissions()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navController?.popBackStack()
                true
            }

            R.id.settingsFragment -> {
                if (navController?.currentDestination?.id == R.id.profileFragment) {
                    navController?.navigate(R.id.action_profileFragment_to_settingsFragment)
                    return true
                }
                return super.onOptionsItemSelected(item)
            }

            else -> {
                navController?.let { NavigationUI.onNavDestinationSelected(item, it) } ?: false || super.onOptionsItemSelected(item)
                true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        // Show the Settings only in the profile page
        val isProfileFragment = navController?.currentDestination?.id == R.id.profileFragment
        menu?.findItem(R.id.settingsFragment)?.isVisible = isProfileFragment // Hide if not in Profile Fragment

        return super.onCreateOptionsMenu(menu)
    }


    private fun navigateToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish() // Closes the AuthActivity so it is removed from the back stack
    }


    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request permissions if not granted
    private fun checkAndRequestLocationPermissions() {
        if (hasLocationPermissions()) {
            Log.d(TAG, "Location permissions already granted. Starting location updates.")
            updateUserLocationIfShared()
        } else {
            Log.d(TAG, "Requesting location permissions...")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d(TAG, "Location permissions granted. Starting location updates.")
                updateUserLocationIfShared()
            } else {
                Log.d(TAG, "Location permissions denied.")
            }
        }
    }

    private val backgroundExecutor = Executors.newCachedThreadPool()
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun updateUserLocationIfShared() {
        // Run this on a background thread to avoid blocking UI
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val authUser = authManager.getCurrentUser() ?: return@launch

                // Get the user data
                Model.shared.getUserById(authUser.uid) { user ->
                    if (user?.isLocationShared == true) {
                        // Start observing location changes
                        GeoUtils.observeLocationChanges(this@MainActivity) { newLocation ->
                            // Update the user's location in the database
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    // Save to the database
                                    Model.shared.editUser(
                                        user,
                                        context = this@MainActivity,
                                        newProfilePicture = null
                                    ) {
                                        Log.d(TAG, "User location updated in DB with new coordinates: Lat=${newLocation.latitude}, Lon=${newLocation.longitude}")
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error updating location: ${e.message}")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateUserLocationIfShared: ${e.message}")
            }
        }
    }

    // Also add this to onDestroy() to prevent memory leaks
    override fun onDestroy() {
        backgroundExecutor.shutdown()
        super.onDestroy()
    }

    private fun checkLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

}