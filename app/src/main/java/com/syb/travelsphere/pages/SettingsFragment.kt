package com.syb.travelsphere.pages

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.syb.travelsphere.auth.AuthActivity
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.databinding.FragmentSettingsBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User
import com.syb.travelsphere.utils.ImagePickerUtil

class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null
    private lateinit var authManager: AuthManager  // Declare AuthManager
    private var currentUserObject: User? = null
    private lateinit var imagePicker: ImagePickerUtil
    private var didSetProfilePicture: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        authManager = AuthManager()  // Initialize AuthManager
        val currentUser = authManager.getCurrentUser()

        // Initialize ImagePickerUtil
        imagePicker = ImagePickerUtil(this) { bitmap ->
            bitmap?.let {
                binding?.profileImage?.setImageBitmap(it)
                didSetProfilePicture = true
            }
        }

        setupListeners()

        // init: fill user data in the form
        currentUser?.uid?.let { userid ->
            Model.shared.getUserById(userid) { fetchedUser ->
                currentUserObject = fetchedUser?.copy()

                binding?.emailText?.setText(currentUser.email)
                binding?.phoneText?.setText(currentUserObject?.phoneNumber)
                binding?.usernameText?.setText(currentUserObject?.userName)
                binding?.locationSwitch?.isChecked = currentUserObject?.isLocationShared == true
                
                if (!currentUserObject?.profilePictureUrl.isNullOrEmpty()) {
                    try {
                        val userProfilePictureUrl = currentUserObject?.profilePictureUrl
                        if (userProfilePictureUrl != null) {
                            Model.shared.getImageByUrl(userProfilePictureUrl) { bitmap ->
                                binding?.profileImage?.setImageBitmap(bitmap)
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "onCreateView: error loading photo")
                    }
                }
            }
        }

        // Make email field unchangeable
        binding?.emailText?.isEnabled = false

        // Handle Edit Button Click
        binding?.editButton?.setOnClickListener() {
            val updatedUser = currentUserObject?.copy(
                phoneNumber = binding?.phoneText?.text.toString().trim(),
                userName =  binding?.usernameText?.text.toString().trim(),
                isLocationShared = binding?.locationSwitch?.isChecked ?: false
            )
            var profilePictureBitmap: Bitmap? = null

            if (didSetProfilePicture) {
                binding?.profileImage?.isDrawingCacheEnabled = true
                binding?.profileImage?.buildDrawingCache()

                val drawable = binding?.profileImage?.drawable
                profilePictureBitmap = (drawable as? BitmapDrawable)?.bitmap
            }

            if (updatedUser != null) {
                editUser(updatedUser, profilePictureBitmap)
            }
        }

        return binding?.root
    }

    private fun editUser(user: User, image: Bitmap?) {
        Model.shared.editUser(user, image,) {
            Toast.makeText(
                requireContext(),
                "Profile updated successfully!",
                Toast.LENGTH_SHORT
            ).show()
            navigateToUserProfile()
        }
    }

    private fun setupListeners() {
        binding?.editProfileButton?.setOnClickListener {
            imagePicker.showImagePickerDialog()
        }

        // Handle Logout Button Click
        binding?.logoutButton?.setOnClickListener() {
            authManager.signOut {}
            navigateToAuthActivity()
        }
    }

    private fun navigateToAuthActivity() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // Closes the Activity so it is removed from the back stack
    }

    private fun navigateToUserProfile() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToProfileFragment()
        binding?.root?.let {
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}