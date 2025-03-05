package com.syb.travelsphere.pages

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.databinding.FragmentSettingsBinding
import com.syb.travelsphere.model.FirebaseModel
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User

class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null
    private lateinit var authManager: AuthManager  // Declare AuthManager
    private val firebaseModel = FirebaseModel()
    private val imagePickerRequestCode = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        authManager = AuthManager()  // Initialize AuthManager
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        setupListeners()
        authManager.getCurrentUser()?.uid?.let {
            Model.shared.getUserById(it) { liveData: LiveData<User> ->
                if (liveData != null && authManager.getCurrentUser() != null) {
                    binding?.emailText?.setText(authManager.getCurrentUser()?.email)
                    binding?.phoneText?.setText(liveData.value?.phoneNumber)
                    binding?.usernameText?.setText(liveData.value?.userName)
                    binding?.locationSwitch?.isChecked = liveData.value?.isLocationShared == true
                }
            }
        }


        // Enable back button in toolbar
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // ✅ Get the current user and display details
        val currentUser = authManager.getCurrentUser()
        if (currentUser != null) {

        }

        // ✅ Handle Logout Button Click
        binding?.logoutButton?.setOnClickListener()
        {
            authManager.signOut {
                requireActivity().finish()  // Close the activity after logging out
            }
        }

        // ✅ Make email field unchangeable
        binding?.emailText?.isEnabled = false

        // ✅ Handle Edit Button Click
        binding?.editButton?.setOnClickListener()
        {
            val updatedPhone = binding?.phoneText?.text.toString().trim()
            val updatedUsername = binding?.usernameText?.text.toString().trim()
            val isLocationShared = binding?.locationSwitch?.isChecked ?: false

            val currentUser = authManager.getCurrentUser()
            if (currentUser != null) {
                firebaseModel.getUserById(currentUser.uid) { user ->
                    if (user != null) {
                        // Create updated user object (email is not modified)
                        val updatedUser = user.copy(
                            phoneNumber = updatedPhone,
                            userName = updatedUsername,
                            isLocationShared = isLocationShared  // ✅ Add location sharing status
                        )


                        updatedUser.profilePictureUrl?.let { it1 ->
                            Model.shared.getImageByUrl(it1) { image ->
                                Model.shared.editUser(
                                    updatedUser,
                                    image,
                                    {})
                            }
                        }
                    }


                    // Show confirmation (optional)
                    Toast.makeText(
                        requireContext(),
                        "Profile updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        return binding?.root
    }

    private fun setupListeners() {
        binding?.editProfileButton?.setOnClickListener {
            // Open gallery to add photos
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, imagePickerRequestCode)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}