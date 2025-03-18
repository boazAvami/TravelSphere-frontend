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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.syb.travelsphere.auth.AuthActivity
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.databinding.ActivityMainBinding
import com.syb.travelsphere.model.Model

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


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

    private fun updateUserLocationIfShared() {
        val authUser = authManager.getCurrentUser()
        authUser?.let {
            Model.shared.getUserById(authUser.uid) { user ->
                if (user?.isLocationShared == true) { // Only update if location sharing is enabled
                    Model.shared.editUser(
                        user,
                        context = this,
                        newProfilePicture = null
                    ) {
                        Log.d(TAG, "User location updated in DB")
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
   * 5. navigation: make the arrow back work  ✅
   * split adapter and view holder ✅
   * get image inside a runnable? ✅
   * in the nearby add a function validation ✅
   * pass in the title the users name in the display user fragment ✅
   16. retrofit placement - place the secrets network API in another place (like with the cloudinary) - SHIRIN
   *17. order posts from the new to the old ✅
   * 18. order posts in desc ✅
   ==============================================
   14. fix the add map centering - after navigating out and then returning it stop working  ✅
   13. make the project pretty *
   9.
   12. a
*/