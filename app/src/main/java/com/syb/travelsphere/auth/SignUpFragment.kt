package com.syb.travelsphere.auth

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import com.syb.travelsphere.MainActivity
import com.syb.travelsphere.R
import com.syb.travelsphere.databinding.FragmentSignUpBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User
import java.io.File
import java.io.InputStream

class SignUpFragment : Fragment() {

    private var binding: FragmentSignUpBinding? = null
    private lateinit var authManager: AuthManager
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var didSetProfilePicture: Boolean = false
    private var profileImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                val inputStream: InputStream? = requireContext().contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                updateProfilePicture(imageUri)
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        // Get email input from signIn page
        val email =  arguments?.let {
            SignUpFragmentArgs.fromBundle(it).email
        }
        binding?.emailEditText?.setText(email)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
                bitmap?.let {
                    binding?.profilePictureImageView?.setImageBitmap(bitmap)
                    binding?.profilePictureImageView?.visibility = View.VISIBLE
                    binding?.addProfilePictureImageButton?.visibility = View.GONE
                    didSetProfilePicture = true
                } ?: Toast.makeText(requireContext(), "Failed to capture photo", Toast.LENGTH_SHORT).show()
        }

        binding?.addProfilePictureImageButton?.setOnClickListener {
            showImagePickerDialog()
        }

        binding?.profilePictureImageView?.setOnClickListener {
            showImagePickerDialog()
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthManager()

        // Sign Up Button Click
        binding?.signUpButton?.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener  // Stop execution if validation fails
            // Get the data from an ImageView as bytes
            binding?.profilePictureImageView?.isDrawingCacheEnabled = true
            binding?.profilePictureImageView?.buildDrawingCache()
//            val profilePictureBitmap = (binding?.profilePictureImageViewToChange?.drawable as BitmapDrawable).bitmap

            val drawable = binding?.profilePictureImageView?.drawable
            val profilePictureBitmap: Bitmap? = when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                else -> null
            }

            val email = binding?.emailEditText?.text.toString().trim()
            val password = binding?.passwordEditText?.text.toString().trim()
            val username = binding?.usernameEditText?.text.toString().trim()
            val phone = binding?.phoneNumberEditText?.text.toString().trim()
            val isLocationShared = binding?.shareLocationSwitch?.isChecked ?: false

//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (password.length < 6) {
//                Toast.makeText(requireContext(), "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }

            if (didSetProfilePicture) {
                signUp(
                    email, password,
                    username = username,
                    phone = phone,
                    isLocationShared = isLocationShared,
                    profilePicture = profilePictureBitmap
                )
            } else
                signUp(
                    email, password,
                    username = username,
                    phone = phone,
                    isLocationShared = isLocationShared,
                    profilePicture = null
                )
        }

        // Navigate to Sign In
        binding?.signInLink?.setOnClickListener {
            val email = binding?.emailEditText?.text.toString().trim()
//            val action = SignUpFragmentDirections.actionSignUpFragmentToSignInFragment(email)
            val action = SignUpFragmentDirections.actionSignUpFragmentToSignInFragment().apply {
                this.email = email
            }
            findNavController().navigate(action)
        }
    }

    private fun showImagePickerDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_image_picker, null)
        view.findViewById<View>(R.id.optionCamera).setOnClickListener {
            bottomSheetDialog.dismiss()
            openCamera()
        }
        view.findViewById<View>(R.id.optionGallery).setOnClickListener {
            bottomSheetDialog.dismiss()
            openGallery()
        }
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun openCamera() {
        if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            cameraLauncher?.launch(null)
        } else {
            // Permission not granted, Request it
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(pickPhotoIntent)
    }

    private fun updateProfilePicture(imageUri: Uri) {
        binding?.profilePictureImageView?.visibility = View.VISIBLE
        binding?.addProfilePictureImageButton?.visibility = View.INVISIBLE

        Picasso.get()
            .load(imageUri)
            .resize(200, 200) // Resize to prevent memory issues
            .centerCrop()
            .into(binding?.profilePictureImageView)

//        binding?.addProfilePictureImageButton?.apply {
//            visibility = View.INVISIBLE    // Make sure it's still visible
//            isClickable = true         // Ensure it's clickable
//            isFocusable = true           // Ensure it can receive touch events
//            bringToFront()             // Move it on top of the image
//        }

        didSetProfilePicture = true
    }

    private fun validateInputs(): Boolean {
        val email = binding?.emailEditText?.text.toString().trim()
        val password = binding?.passwordEditText?.text.toString().trim()
        val username = binding?.usernameEditText?.text.toString().trim()
        val phone = binding?.phoneNumberEditText?.text.toString().trim()

        var isValid = true

        // **Email Validation**
        if (email.isEmpty()) {
            binding?.emailInputLayout?.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding?.emailInputLayout?.error = "Invalid email format"
            isValid = false
        } else {
            binding?.emailInputLayout?.error = null
        }

        // **Password Validation**
        if (password.isEmpty()) {
            binding?.passwordInputLayout?.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding?.passwordInputLayout?.error = "Password must be at least 6 characters long"
            isValid = false
        } else {
            binding?.passwordInputLayout?.error = null
        }

        // **Username Validation**
        if (username.isEmpty()) {
            binding?.usernameInputLayout?.error = "Username is required"
            isValid = false
        } else {
            binding?.usernameInputLayout?.error = null
        }

        // **Phone Number Validation**
        if (phone.isEmpty()) {
            binding?.phoneNumberInputLayout?.error = "Phone number is required"
            isValid = false
        } else if (!phone.matches(Regex("^[0-9]{9,15}$"))) {
            binding?.phoneNumberInputLayout?.error = "Invalid phone number format"
            isValid = false
        } else {
            binding?.phoneNumberInputLayout?.error = null
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
        profilePicture?.let {
            authManager.signUpUser(
                email,
                password,
                username = username,
                phone = phone,
                isLocationShared = isLocationShared,
                profilePicture = it
            ) { user ->
                if (user != null) {
                    Toast.makeText(requireContext(), "Sign up successful!", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(requireContext(), "Sign up failed.", Toast.LENGTH_SHORT).show()
                }
            }
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
