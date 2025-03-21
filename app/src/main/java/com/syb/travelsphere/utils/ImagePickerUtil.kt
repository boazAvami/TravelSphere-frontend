package com.syb.travelsphere.utils

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class ImagePickerUtil(
    private val fragment: Fragment,
    private val callback: (Bitmap?) -> Unit
) {
    private var requestedPermission: String? = null // Tracks whether the app is requesting camera or gallery access.

    // Permission Request for Camera and Gallery
    private val permissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            when (requestedPermission) {
                Manifest.permission.CAMERA -> cameraLauncher.launch(null)
                Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE -> launchGalleryPicker()
            }
        } else {
            Toast.makeText(fragment.requireContext(), "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show()
        }
    }


    // Open Gallery
    private val galleryLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(fragment.requireContext().contentResolver, uri)
                        callback(bitmap) // Correctly returns only the selected image
                    } catch (e: Exception) {
                        Toast.makeText(fragment.requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(fragment.requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

    // Open Camera
    private val cameraLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            callback(bitmap) // Directly return the captured bitmap
        }

    fun showImagePickerDialog() {
        val options = arrayOf("Take a Photo", "Choose from Gallery")

        android.app.AlertDialog.Builder(fragment.requireContext())
            .setTitle("Select Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    fun openCamera() {
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.CAMERA)
            == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            cameraLauncher.launch(null) // Open camera if permission is granted
        } else {
            requestedPermission = Manifest.permission.CAMERA
            permissionLauncher.launch(Manifest.permission.CAMERA) // Request permission
        }
    }

    fun openGallery() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES // Request full media access for Android 13+
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE // Request full storage access for older Android versions
        }

        if (ContextCompat.checkSelfPermission(fragment.requireContext(), permission)
            == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            launchGalleryPicker() // Open gallery if permission is granted
        } else {
            requestedPermission = permission
            permissionLauncher.launch(permission) // Request full access
        }
    }

    private val photoPickerLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(fragment.requireContext().contentResolver, uri)
                    callback(bitmap) // Returns only the selected image
                } catch (e: Exception) {
                    Toast.makeText(fragment.requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(fragment.requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

    private fun launchGalleryPicker() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(pickPhotoIntent)
    }


    fun convertBitmapToBase64(bitmap: android.graphics.Bitmap): String {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return "data:image/jpeg;base64," + android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }
}
