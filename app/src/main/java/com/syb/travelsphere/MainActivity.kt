package com.syb.travelsphere

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.syb.travelsphere.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.model.User


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //TODO: Test to delete

//        testDatabase()
//        testFirestore()

        // Call the function to test authentication
//        authManager = AuthManager()
//        testAuthFunctions()
        //TODO: Until here to delete


        // Set up the Toolbar (ActionBar)
        setSupportActionBar(binding.toolbar) // This sets the toolbar as the ActionBar

        // Set up NavController with BottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.let {
            NavigationUI.setupActionBarWithNavController(
                activity = this,
                navController = it
            )
        }

        navController.let { NavigationUI.setupWithNavController(binding.bottomNavigationView, it) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navController.popBackStack()
                true
            }
            else -> NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
        }
    }
//
//    private fun testAuthFunctions() {
//        val testEmail = "testuser@example.com"
//        val testPassword = "password123"
//
//        Log.d("AuthTest", "Starting authentication tests...")
//
//        // Step 1: Sign up a user
//        authManager.signUpUser(testEmail, testPassword) { user ->
//            if (user != null) {
//                Log.d("AuthTest", "✅ Sign Up Success: ${user.email}")
//            } else {
//                Log.e("AuthTest", "❌ Sign Up Failed")
//            }
//
//            // Step 2: Sign in the user
//            authManager.signInUser(testEmail, testPassword) { loggedInUser ->
//                if (loggedInUser != null) {
//                    Log.d("AuthTest", "✅ Sign In Success: ${loggedInUser.email}")
//                } else {
//                    Log.e("AuthTest", "❌ Sign In Failed")
//                }
//
//                // Step 3: Check if user is logged in
//                val isLoggedIn = authManager.isUserLoggedIn()
//                Log.d("AuthTest", "🔍 Is User Logged In? $isLoggedIn")
//
//                // Step 4: Get current user
//                val currentUser = authManager.getCurrentUser()
//                Log.d("AuthTest", "👤 Current User: ${currentUser?.email ?: "No User"}")
//
//                // Step 5: Sign out
//                authManager.signOut {
//                    Log.d("AuthTest", "🚪 User Signed Out")
//                    Log.d(
//                        "AuthTest",
//                        "🔍 Is User Logged In after sign out? ${authManager.isUserLoggedIn()}"
//                    )
//                }
//            }
//        }
//    }

//    fun testFirestore() {
//        val db = Firebase.firestore
//        val user = hashMapOf(
//            "first" to "yael",
//            "last" to "Hamami",
//            "born" to 2001
//        )
//
//        val user2 = hashMapOf(
//            "email" to "john2222@example.com",
//            "profilePictureUrl" to "",
//            "userName" to "John Doe",
//            "password" to "123",
//           "phoneNumber" to "0123",
//            "isLocationShared" to true
//        )
//
//        db.collection("users-test")
//            .add(user2).addOnSuccessListener { documentReference ->
//                Log.d("TAG", "added with id: ${documentReference.id}")
//            }
//    }
//
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
//       )) {
//            Log.d("DatabaseTest", "Post added successfully!")
//
//            // Fetch posts
//            Model.shared.getAllPosts { posts ->
//                Log.d("DatabaseTest", "Retrieved posts: $posts")
//            }
//        }
//    }
}