package com.syb.travelsphere

import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.syb.travelsphere.databinding.ActivityMainBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.model.User

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //TODO: Test to delete

//        testDatabase()
//        testFirestore()
        //TODO: Until here to delete


        // Set up the Toolbar (ActionBar)
        setSupportActionBar(binding.toolbar) // This sets the toolbar as the ActionBar

        // Set up NavController with BottomNavigationView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Configure AppBarConfiguration for top-level destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.allPostsFragment,
                R.id.nearbyUsersFragment,
                R.id.newPostFragment,
                R.id.profileFragment
            )
        )

        // Link NavController with Toolbar and BottomNavigationView
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)

        // Handle Back Stack for Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.allPostsFragment -> navController.navigate(R.id.allPostsFragment)
                R.id.nearbyUsersFragment -> navController.navigate(R.id.nearbyUsersFragment)
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
//
//    fun testFirestore() {
//        val db = Firebase.firestore
//        val user = hashMapOf(
//            "first" to "yael",
//            "last" to "Hamami",
//            "born" to 2001
//        )
//
//        val user2 = hashMapOf(
//            "email" to "john@example.com",
//            "profilePictureUrl" to "",
//            "userName" to "John Doe",
//            "password" to "123",
//            "phoneNumber" to "0123",
//            "isLocationShared" to true
//        )
//
//        db.collection("users-test")
//            .add(user2).addOnSuccessListener { documentReference ->
//                Log.d("TAG", "added with id: ${documentReference.id}")
//            }
//    }
//    fun testDatabase() {
//        // Add a user and test
//        Model.shared.addUser(User(
//            email = "john@example.com",
//            profilePictureUrl = "",
//            userName = "John Doe",
//            password = "123",
//            phoneNumber = "0123",
//            isLocationShared = true
//        )) {
//            Log.d("DatabaseTest", "User added successfully!")
//
//            // Fetch users
//            Model.shared.getAllUsers { users ->
//                Log.d("DatabaseTest", "Retrieved users: $users!")
//            }
//        }
//
//// Add a post and test
//        Model.shared.addPost(Post(
//            title = "First Post",
//            description = "First Post Description",
//            imageUrl = "",
////            location = ,
//            ownerId = 1
//        )) {
//            Log.d("DatabaseTest", "Post added successfully!")
//
//            // Fetch posts
//            Model.shared.getAllPosts { posts ->
//                Log.d("DatabaseTest", "Retrieved posts: $posts")
//            }
//        }
//    }
}