// MainActivity.kt
package com.syb.travelsphere

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.syb.travelsphere.pages.AllPostsFragment
import com.syb.travelsphere.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load the AllPostsFragment by default
        if (savedInstanceState == null) {
            loadFragment(AllPostsFragment())
        }

        // Set up BottomNavigationView with NavController
        // val navController = findNavController(R.id.nav_host_fragment)
        // val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        // bottomNavigationView.setupWithNavController(navController)

        val navController = findNavController(R.id.nav_host_fragment) // Use the correct fragment ID
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
