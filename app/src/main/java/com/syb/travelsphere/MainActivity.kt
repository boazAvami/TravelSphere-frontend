package com.syb.travelsphere

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.auth.AuthActivity
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.databinding.ActivityMainBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.utils.GeoUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var navController: NavController? = null
    private lateinit var authManager: AuthManager

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val SIGNIFICANT_DISTANCE_THRESHOLD_KM = 0.5
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
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set up the Toolbar (ActionBar)
        val toolBar: Toolbar = findViewById(R.id.mainToolbar)
        setSupportActionBar(toolBar)


        val navHostController: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as? NavHostFragment
        if(navHostController != null) {
            Log.d(TAG, "onCreate: test")
            navController = navHostController?.navController
            navController?.let {
                NavigationUI.setupActionBarWithNavController(
                    activity = this,
                    navController = it
                )
            }
        } else {
            Log.e(TAG, "NavHostFragment not found! Check if R.id.mainNavHostFragment is correct.")

        }


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.mainBottomNavigationBar)
        navController?.let { NavigationUI.setupWithNavController(bottomNavigationView, it) }

        // Dynamically show/hide settings menu based on current fragment
        navController?.addOnDestinationChangedListener { _, destination, _ ->
            invalidateOptionsMenu() // Refresh the menu when navigation changes
            Log.d(TAG, "Navigated to: ${destination.label}")
            logFragmentBackStack()
        }

        checkAndRequestLocationPermissions()
    }

    override fun onSupportNavigateUp(): Boolean {
        val result = navController?.navigateUp() == true || super.onSupportNavigateUp()
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ${logFragmentBackStack()}")

        return when (item.itemId) {
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: ${logFragmentBackStack()}")
                navController?.popBackStack()
                true
            }
            else -> {
//                return super.onOptionsItemSelected(item)
                navController?.popBackStack()
                Log.d(TAG, "onOptionsItemSelected: ${logFragmentBackStack()}")
                navController?.let { NavigationUI.onNavDestinationSelected(item, it) }

                true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG, "onBackPressed: the state go to ${logFragmentBackStack()}")
    }

    private fun navigateToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish() // Closes the AuthActivity so it is removed from the back stack
    }

    private fun logFragmentBackStack() {
        val fragmentManager = supportFragmentManager
        val count = fragmentManager.backStackEntryCount
        Log.d(TAG, "BackStack - Fragment Back Stack Count: $count")

        for (i in 0 until count) {
            val entry = fragmentManager.getBackStackEntryAt(i)
            Log.d(TAG, "BackStack - Fragment $i: ${entry.name}")
        }
    }

//        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu, menu)
//
//        // Show the Settings only in the profile page
//        val isProfileFragment = navController?.currentDestination?.id == R.id.profileFragment
//        menu?.findItem(R.id.settingsFragment)?.isVisible = isProfileFragment // Hide if not in Profile Fragment
////
//        // Show the Up button only in the Settings page
//        val isSettingsFragment = navController?.currentDestination?.id == R.id.settingsFragment
//        supportActionBar?.setDisplayHomeAsUpEnabled(isSettingsFragment)
//
//        return super.onCreateOptionsMenu(menu)
//    }

    private fun checkLocationPermissionsAndUpdate() {
        if (hasLocationPermissions()) {
            updateUserLocationIfShared()
        } else {
            requestLocationPermissions()
        }
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

    private fun updateUserLocationIfShared() {
        val authUser = authManager.getCurrentUser()
        authUser?.let {
            Model.shared.getUserById(authUser.uid) { user ->
                if (user?.isLocationShared == true) { // Only update if location sharing is enabled
//                    GeoUtils.getCurrentLocation(this) { geoPoint ->
//                        if (geoPoint != null) {
//                            val updatedUser = user.copy(
//                                location = geoPoint,
//                                geoHash = GeoUtils.generateGeoHash(geoPoint)
//                            )
                            Model.shared.editUser(
                                user,
                                context = this,
                                newProfilePicture = null
                            ) {
                                Log.d(TAG, "User location updated in DB")
                            }
//                        } else {
//                            Log.e("MainActivity", "Failed to get location for update")
//                        }
//                    }
                }
            }
        }
    }

    private fun updateUserLocation(newLocation: GeoPoint) {
            val authUser = authManager.getCurrentUser() // Fetch logged-in user
            if (authUser != null) {
                Model.shared.getUserById(authUser.uid) { user ->
                    if (user != null && user.isLocationShared == true) {
                        val newGeoHash = GeoUtils.generateGeoHash(newLocation)

                        val updatedUser = user.copy(
                            location = newLocation,
                            geoHash = newGeoHash
                        )

                        Model.shared.editUser(
                            updatedUser,
                            newProfilePicture = null,
                            context = this,
                        ) {
                            Log.d(TAG, "User location updated in Firestore: $newLocation")
                        }
                    }
                }
            }
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

/*TODO:
*  - me:
*  1. make permissions ask before camera, gallery photo and gps ✅ - fix the limited access to gallery.
*  2. using islocation with others shared from db - add to the query. ✅
   4. add updating current location in db ✅
   8. make input validation an util - in settings page and edit post ✅
   6. add view other user page ✅
   7. add delete post button ✅
   14. fix nearby users bug with the distance radius ✅
   * add onFailure to everything and error handling ✅
   * add loading circle ✅
   5. navigation: make the arrow back work ??
   16. retrofit placement - place the secrets network API in another place (like with the cloudinary) - with boaz -
   ==============================================
   14. fix the add map centering - after navigating out and then returning it stop working ??
   13. make the project pretty *
   9.
   12. a


*/