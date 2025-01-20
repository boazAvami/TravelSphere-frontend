package com.syb.travelsphere

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.syb.travelsphere.pages.AllPostsFragment
import androidx.appcompat.widget.Toolbar
import com.syb.travelsphere.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setContentView(R.layout.activity_main)

        // Set up the Toolbar (ActionBar)
        setSupportActionBar(binding.toolbar) // This sets the toolbar as the ActionBar

        // Set up NavController with BottomNavigationView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.allPostsFragment,
                R.id.nearbyTravellersFragment,
                R.id.newPostFragment,
                R.id.profileFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up BottomNavigationView with NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // Handle Back Stack for Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.allPostsFragment -> navController.navigate(R.id.allPostsFragment)
                R.id.nearbyTravellersFragment -> navController.navigate(R.id.nearbyTravellersFragment)
                R.id.newPostFragment -> navController.navigate(R.id.newPostFragment)
                R.id.profileFragment -> navController.navigate(R.id.profileFragment)
                else -> false
            }
            true
        }

        // Handle back button navigation
        onBackPressedDispatcher.addCallback(this) {
            if (navController.currentDestination?.id == R.id.allPostsFragment) {
                finish() // Exit app if on the start destination
            } else {
                navController.popBackStack() // Navigate back
            }
        }
    }

    // Override onSupportNavigateUp to handle up navigation
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
