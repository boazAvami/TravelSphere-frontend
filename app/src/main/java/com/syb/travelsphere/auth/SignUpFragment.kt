package com.syb.travelsphere.auth

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import com.syb.travelsphere.MainActivity
import com.syb.travelsphere.databinding.FragmentSignUpBinding
import com.syb.travelsphere.utils.ImagePickerUtil
import com.syb.travelsphere.utils.InputValidator

class SignUpFragment : Fragment() {

    private var binding: FragmentSignUpBinding? = null
    private lateinit var authManager: AuthManager

    private var didSetProfilePicture: Boolean = false
    private lateinit var imagePicker: ImagePickerUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        // Get email input from signIn page
        val email =  arguments?.let {
            SignUpFragmentArgs.fromBundle(it).email
        }
        binding?.emailEditText?.setText(email ?: "")

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthManager()

        // Initialize ImagePickerUtil
        imagePicker = ImagePickerUtil(this) { bitmap ->
            bitmap?.let {
                binding?.profilePictureImageView?.setImageBitmap(it)
                binding?.profilePictureImageView?.visibility = View.VISIBLE
                binding?.addProfilePictureImageButton?.visibility = View.GONE
                didSetProfilePicture = true
            }
        }

        binding?.addProfilePictureImageButton?.setOnClickListener {
            imagePicker.showImagePickerDialog()
        }

        binding?.profilePictureImageView?.setOnClickListener {
            imagePicker.showImagePickerDialog()
        }

        // Sign Up Button Click
        binding?.signUpButton?.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener  // Stop execution if validation fails
            // Get the data from an ImageView as bytes
            binding?.profilePictureImageView?.isDrawingCacheEnabled = true
            binding?.profilePictureImageView?.buildDrawingCache()

            val drawable = binding?.profilePictureImageView?.drawable
            val profilePictureBitmap: Bitmap? = (drawable as? BitmapDrawable)?.bitmap

            val email = binding?.emailEditText?.text.toString().trim()
            val password = binding?.passwordEditText?.text.toString().trim()
            val username = binding?.usernameEditText?.text.toString().trim()
            val phone = binding?.phoneNumberEditText?.text.toString().trim()
            val isLocationShared = binding?.shareLocationSwitch?.isChecked ?: false

            signUp(email = email,
                password = password,
                username = username,
                phone = phone,
                isLocationShared = isLocationShared,
                profilePicture = profilePictureBitmap
            )
        }

        // Navigate to Sign In
        binding?.signInLink?.setOnClickListener {
            val email = binding?.emailEditText?.text.toString().trim()
            val action = SignUpFragmentDirections.actionSignUpFragmentToSignInFragment().apply {
                this.email = email
            }
            findNavController().navigate(action)
        }
    }

    private fun showCameraPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Camera Permission Required")
            .setMessage("To take a photo, enable the camera permission in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfilePicture(imageUri: Uri) {
        Picasso.get()
            .load(imageUri)
            .resize(200, 200) // Resize to prevent memory issues
            .centerCrop()
            .into(binding?.profilePictureImageView)

        binding?.profilePictureImageView?.visibility = View.VISIBLE
        binding?.addProfilePictureImageButton?.visibility = View.INVISIBLE
        didSetProfilePicture = true
    }

    private fun validateInputs(): Boolean {
        val email = binding?.emailEditText?.text.toString().trim()
        val password = binding?.passwordEditText?.text.toString().trim()
        val username = binding?.usernameEditText?.text.toString().trim()
        val phone = binding?.phoneNumberEditText?.text.toString().trim()

        var isValid = true

        if (!InputValidator.validateEmail(email, binding?.emailInputLayout)) {
            isValid = false
        }
        if (!InputValidator.validatePassword(password, binding?.passwordInputLayout)) {
            isValid = false
        }
        if (!InputValidator.validateUsername(username, binding?.usernameInputLayout)) {
            isValid = false
        }
        if (!InputValidator.validatePhoneNumber(phone, binding?.phoneNumberInputLayout)) {
            isValid = false
        }

        return isValid
    }

    private fun signUp(email: String,
                       password: String,
                       username: String,
                       phone: String,
                       isLocationShared: Boolean,
                       profilePicture: Bitmap?
                       ) {
        binding?.progressBar?.visibility = View.VISIBLE
        authManager.signUpUser(email, password, username, phone, isLocationShared, profilePicture) { user ->
            if (user != null) {
                Toast.makeText(requireContext(),"Sign up successful!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                Toast.makeText(requireContext(),"Sign up failed.", Toast.LENGTH_SHORT).show()
            }

            binding?.progressBar?.visibility = View.GONE
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // Close AuthActivity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
