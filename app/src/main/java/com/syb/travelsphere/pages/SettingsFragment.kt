package com.syb.travelsphere.pages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null
    private lateinit var authManager: AuthManager  // Declare AuthManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        authManager = AuthManager()  // Initialize AuthManager
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Enable back button in toolbar
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // ✅ Get the current user and display details
        val currentUser = authManager.getCurrentUser()
        if (currentUser != null) {
            binding?.emailText?.setText(currentUser.email)
            binding?.phoneText?.setText(currentUser.phoneNumber ?: "No phone number")
            binding?.usernameText?.setText(currentUser.displayName ?: "No username")
        }

        // ✅ Handle Logout Button Click
        binding?.logoutButton?.setOnClickListener {
            authManager.signOut {
                requireActivity().finish()  // Close the activity after logging out
            }
        }

        return binding?.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}