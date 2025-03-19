package com.syb.travelsphere.pages

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.syb.travelsphere.R
import com.syb.travelsphere.databinding.FragmentEditPostBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.utils.ImagePickerUtil
import com.syb.travelsphere.utils.InputValidator
import com.syb.travelsphere.utils.TimeUtil.formatTimestamp

class EditPostFragment : Fragment() {
    private var binding: FragmentEditPostBinding? = null
    private lateinit var post: Post
    private var postId: String? = null
    private val viewModel: ProfileViewModel by viewModels()
    private var selectedImageBitmap: Bitmap? = null
    private var imageChanged = false
    private lateinit var imagePickerUtil: ImagePickerUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditPostBinding.inflate(inflater, container, false)

        postId = arguments?.let {
            SinglePostFragmentArgs.fromBundle(it).postId
        }

        // Initialize ImagePickerUtil with the callback to handle selected images
        imagePickerUtil = ImagePickerUtil(this) { bitmap ->
            if (bitmap != null) {
                selectedImageBitmap = bitmap
                imageChanged = true
                binding?.photoViewPager?.setImageBitmap(bitmap)

                // Show confirmation that image has been updated
                Toast.makeText(requireContext(), "Image updated successfully", Toast.LENGTH_SHORT).show()
            }
        }

        getPost()

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (postId != null) {
            fillPostData()
        }

        setupImageClickListener()
        setupButtons()
    }

    private fun setupButtons() {
        // Apply material design elevation and shadow effects
        binding?.editButton?.apply {
            elevation = 8f
            setOnClickListener {
                if (!validateInputs()) return@setOnClickListener
                showConfirmationDialog("Edit Post", "Are you sure you want to save these changes?") {
                    editPost()
                }
            }
        }

        binding?.deleteButton?.apply {
            setOnClickListener {
                showConfirmationDialog("Delete Post", "Are you sure you want to delete this post? This action cannot be undone.") {
                    deletePost()
                }
            }
        }
    }

    private fun setupImageClickListener() {
        // Make the image card clickable to edit
        binding?.photoViewPagerMaterialCardView?.setOnClickListener {
            imagePickerUtil.showImagePickerDialog()
        }

        // Also make the edit indicator clickable separately
        binding?.editImageIndicator?.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                imagePickerUtil.showImagePickerDialog()
            }
        }
    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Confirm") { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .show()
    }

    private fun editPost() {
        val newDescription = binding?.descriptionText?.text.toString()
        val newLocationName = binding?.locationNameText?.text.toString().trim()

        binding?.progressBar?.visibility = View.VISIBLE
        binding?.buttonsLayout?.visibility = View.INVISIBLE

        // Create a copy of the post with updated fields
        val newPost = post.copy(
            description = newDescription,
            locationName = newLocationName
        )

        // Use a background thread for image processing
        Thread {
            try {
                // Handle image update if changed
                if (imageChanged && selectedImageBitmap != null) {
                    // Upload the new image using Model's uploadImage method
                    Model.shared.uploadImage(selectedImageBitmap!!) { imageUrl ->
                        if (imageUrl != null) {
                            // Create a new photo list with the updated image url as the first element
                            val updatedPhotos = post.photos.toMutableList()
                            if (updatedPhotos.isNotEmpty()) {
                                updatedPhotos[0] = imageUrl
                            } else {
                                updatedPhotos.add(imageUrl)
                            }

                            // Update post with new photos list
                            val updatedPost = newPost.copy(photos = updatedPhotos)
                            updatePostInDatabase(updatedPost)
                        } else {
                            // Handle image upload failure
                            Handler(Looper.getMainLooper()).post {
                                binding?.progressBar?.visibility = View.GONE
                                binding?.buttonsLayout?.visibility = View.VISIBLE
                                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // No image change, just update the text fields
                    updatePostInDatabase(newPost)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    binding?.progressBar?.visibility = View.GONE
                    binding?.buttonsLayout?.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Error updating post", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun updatePostInDatabase(newPost: Post) {
        Model.shared.editPost(newPost) {
            Handler(Looper.getMainLooper()).post {
                viewModel.notifyPostModified()
                binding?.progressBar?.visibility = View.GONE
                view?.let { Navigation.findNavController(it).popBackStack() }
                Toast.makeText(requireContext(), "Post updated successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePost() {
        binding?.progressBar?.visibility = View.VISIBLE
        binding?.buttonsLayout?.visibility = View.INVISIBLE

        // Run deletion in background thread
        Thread {
            try {
                Model.shared.deletePost(post) {
                    Handler(Looper.getMainLooper()).post {
                        viewModel.notifyPostModified()
                        binding?.progressBar?.visibility = View.GONE
                        view?.let { Navigation.findNavController(it).popBackStack() }
                        Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting post: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    binding?.progressBar?.visibility = View.GONE
                    binding?.buttonsLayout?.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Error deleting post", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun getPost() {
        Log.d(TAG, "getPost: fetching post data")

        // Run data fetching in background thread
        Thread {
            try {
                postId?.let { id ->
                    Model.shared.getPostById(id) {
                        Log.d(TAG, "getPost result: $it")
                        if (it != null) {
                            this.post = it
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching post: ${e.message}")
            }
        }.start()
    }

    private fun fillPostData() {
        binding?.progressBar?.visibility = View.VISIBLE

        // Run data fetching in background thread
        Thread {
            try {
                postId?.let { postId ->
                    Model.shared.getPostById(postId) { post ->
                        post?.ownerId?.let { ownerId ->
                            Model.shared.getUserById(ownerId) { user ->
                                // Update UI with user info on main thread
                                Handler(Looper.getMainLooper()).post {
                                    binding?.userNameText?.text = user?.userName
                                }

                                if (!user?.profilePictureUrl.isNullOrEmpty()) {
                                    try {
                                        val userProfilePictureUrl = user?.profilePictureUrl
                                        if (userProfilePictureUrl != null) {
                                            Model.shared.getImageByUrl(userProfilePictureUrl) { bitmap ->
                                                // Update profile image on main thread
                                                Handler(Looper.getMainLooper()).post {
                                                    binding?.userProfilePicture?.setImageBitmap(bitmap)
                                                }
                                            }
                                        } else {
                                            Handler(Looper.getMainLooper()).post {
                                                binding?.userProfilePicture?.setImageResource(R.drawable.profile_icon)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Handler(Looper.getMainLooper()).post {
                                            binding?.userProfilePicture?.setImageResource(R.drawable.profile_icon)
                                        }
                                    }
                                }
                            }
                        }

                        // Update post data on main thread
                        Handler(Looper.getMainLooper()).post {
                            binding?.locationNameText?.setText(post?.locationName)
                            binding?.descriptionText?.setText(post?.description)
                            binding?.timestampText?.text = "Created on: ${post?.creationTime?.let { formatTimestamp(it) }}"
                        }

                        post?.photos?.get(0).let { photoUrl ->
                            if (photoUrl != null) {
                                Model.shared.getImageByUrl(photoUrl) { image ->
                                    // Update post image and hide progress on main thread
                                    Handler(Looper.getMainLooper()).post {
                                        binding?.photoViewPager?.setImageBitmap(image)
                                        binding?.progressBar?.visibility = View.GONE
                                    }
                                }
                            } else {
                                Handler(Looper.getMainLooper()).post {
                                    binding?.progressBar?.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading post data: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    binding?.progressBar?.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error loading post data", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    fun validateInputs(): Boolean {
        val description = binding?.descriptionText?.text.toString().trim()
        val locationName = binding?.locationNameText?.text.toString().trim()

        var isValid = true

        if (!InputValidator.validateDescription(description, binding?.descriptionTextTextInputLayout)) {
            isValid = false
        }
        if (!InputValidator.validateRequiredTextField(locationName, binding?.locationNameTextTextInputLayout)) {
            isValid = false
        }

        return isValid
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        private const val TAG = "EditPostFragment"
    }
}