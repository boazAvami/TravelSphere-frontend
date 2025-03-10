package com.syb.travelsphere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.databinding.ActivityMainBinding
import com.syb.travelsphere.auth.AuthActivity
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.utils.GeoUtils.generateGeoHash


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var navController: NavController? = null
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authManager = AuthManager()
//        authManager.signOut {  }

        if (!authManager.isUserLoggedIn()) {
            // Redirect to AuthActivity
            navigateToAuthActivity()
            return
        }

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the Toolbar (ActionBar)
        setSupportActionBar(binding.toolbar) // This sets the toolbar as the ActionBar

        // ✅ Enable the Up button in ActionBar (important!)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set up NavController with BottomNavigationView
        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as? NavHostFragment
        navController = navHostFragment?.navController

        navController?.let {
            NavigationUI.setupActionBarWithNavController(this, it)
            NavigationUI.setupWithNavController(binding.bottomNavigationView, it)
        }

        // ✅ Dynamically show/hide settings menu based on current fragment
        navController?.addOnDestinationChangedListener { _, destination, _ ->
            invalidateOptionsMenu() // Refresh the menu when navigation changes
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val currentDestination = navController?.currentDestination?.id

            if (currentDestination != item.itemId) {
                navController?.navigate(item.itemId)
            }
            true
        }
    }

    private fun navigateToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish() // Closes the AuthActivity so it is removed from the back stack
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (navController?.navigateUp() == true) {
            true
        } else {
            super.onBackPressedDispatcher.onBackPressed() // Fallback if navigation fails
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        // Show the Settings only in the profile page
        val isProfileFragment = navController?.currentDestination?.id == R.id.profileFragment
        menu?.findItem(R.id.settingsFragment)?.isVisible = isProfileFragment // Hide if not in Profile Fragment

        // Show the Up button only in the Settings page
        val isSettingsFragment = navController?.currentDestination?.id == R.id.settingsFragment
        supportActionBar?.setDisplayHomeAsUpEnabled(isSettingsFragment)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navController?.popBackStack()
                true
            }
            else -> {
                navController?.let { NavigationUI.onNavDestinationSelected(item, it) }
                true
            }
        }
    }
}

/*TODO:
*  - me:
   2. add all the functions for posts that needed  - added just check that maybe there will be a change with a geohash and add callbacks
   3. update to work with geo hash
   4. model view to my fragments
   5. add using is location shared from db
   *
   2. make image util  ✅
   3. make input validation an util  ✅
   4. make signUp profile picture appear ✅
   5. make the arrow back work ✅
   6. add onFailure to everything and error handling ✅
   7. add input validation ✅
   8. migrate users collections and authentication work together ✅
   9. add storage (for images) - and work with the posts ✅
   =====================================================================================================================
*   - With boaz and shirin:
   8. GPS To get localization of the phone using the app - in the add new posts and in the main activity (for the nearby)
   * 9. add loading circle*/