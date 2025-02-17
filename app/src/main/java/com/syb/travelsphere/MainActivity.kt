package com.syb.travelsphere

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.syb.travelsphere.databinding.ActivityMainBinding
import com.syb.travelsphere.auth.AuthActivity
import com.syb.travelsphere.auth.AuthManager


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var navController: NavController? = null
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authManager = AuthManager()

        authManager.signOut {  }

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

        // Set up NavController with BottomNavigationView
        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as? NavHostFragment
        navController = navHostFragment?.navController
        navController?.let {
            NavigationUI.setupActionBarWithNavController(
                activity = this,
                navController = it
            )
        }

        navController?.let {
            NavigationUI.setupWithNavController(binding.bottomNavigationView, it)
        }
    }

    private fun navigateToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish() // Closes the AuthActivity so it is removed from the back stack
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
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
   1. make signUp profile picture appear. - IN PROGRESS
   2. add onFailure to everything and error handling ✅
   3. pass as an argument in each navigation (thinking) - a. addPost_to_all_posts (loading upload)
   4. make the arrow back work
   5. add all the functions for posts that needed  - added just check that maybe there will be a change with a geohash and add callbacks
   6. update to work with geo hash
   *
   7. add input validation ✅
   8. migrate users collections and authentication work together ✅
   9. add storage (for images) - and work with the posts ✅
   =====================================================================================================================
*   - With boaz and shirin:
*  7. NEED TO IMPLEMETE CHASH + (ViewModel, Live Data, ROOM) ???
   8. GPS To get localization of the phone using the app - in the add new posts and in the main activity (for the nearby)
   * 9. add loading circle
   * 10. in the addpost save the picture to the storage and the rest in the database*/