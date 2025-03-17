package com.syb.travelsphere.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Closes the AuthActivity so it is removed from the back stack
    }

}
