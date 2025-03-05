package com.syb.travelsphere.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.syb.travelsphere.R
import com.syb.travelsphere.databinding.ActivityAuthBinding
import com.syb.travelsphere.MainActivity

class AuthActivity : AppCompatActivity() {
    private var binding: ActivityAuthBinding? = null
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authManager = AuthManager()

        // Check if user is already logged in and navigate to MainActivity
        if (authManager.isUserLoggedIn()) {
            navigateToMainActivity()
            return
        }

        // Initialize View Binding
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding?.root)

//        ViewCompat.setOnApplyWindowInsetsListener(binding?.navHostFragment) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Closes the AuthActivity so it is removed from the back stack
    }

}
