package com.syb.travelsphere.components

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.R
import com.syb.travelsphere.databinding.ItemUserBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User

class UserViewHolder(
    binding: ItemUserBinding,
    private val onUserClick: (User) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private var user: User? = null
    private val userNameTextView: TextView = binding.userName
    private val userOriginTextView: TextView = binding.userOrigin
    private val userProfilePictureImageView: ImageView =  binding.userProfilePicture

    companion object {
        private const val TAG = "UserViewHolder"
    }

    fun bind(user: User) {
        this.user = user

        // Set the username and phoneNumber
        userNameTextView.text = user.userName
        userOriginTextView.text = user.phoneNumber

        userProfilePictureImageView.setImageResource(R.drawable.default_user)

        if (!user.profilePictureUrl.isNullOrEmpty()) {
            loadProfilePicture(user.profilePictureUrl)

            itemView.setOnClickListener {
                this.user?.let { onUserClick(it) }
            }
//            try {
//                val userProfilePictureUrl = user.profilePictureUrl
//                if (userProfilePictureUrl != null) {
//                    userProfilePictureImageView?.let { imageView ->
//                        Model.shared.getImageByUrl(userProfilePictureUrl) { bitmap ->
//                            imageView.setImageBitmap(bitmap)
//                        }
//                    }
//                } else {
//                    // If decoding fails, set a default image
//                    userProfilePictureImageView?.setImageResource(R.drawable.default_user)
//                }
//            } catch (e: Exception) {
//                // If decoding fails, set a default image
//                userProfilePictureImageView?.setImageResource(R.drawable.default_user)
//            }
        }

        // Handle item click
        itemView.setOnClickListener {
            this.user?.let { onUserClick(it) }
        }
    }

    private fun loadProfilePicture(imageUrl: String?) {
        try {
            // Use high priority loading for profile pictures
            if (imageUrl != null) {
                Model.shared.getImageByUrl(imageUrl) { bitmap ->
                    // Verify the user hasn't changed during async loading
                    if (user?.profilePictureUrl == imageUrl && bitmap != null) {
                        // Run on UI thread to ensure immediate update
                        itemView.post {
                            userProfilePictureImageView.setImageBitmap(bitmap)
                            // Force layout update to ensure image is visible
                            userProfilePictureImageView.invalidate()
                        }
                    } else {
                        Log.d(TAG, "Skipping image update - user changed or bitmap null")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile picture: ${e.message}")
            userProfilePictureImageView.setImageResource(R.drawable.default_user)
        }
    }
}