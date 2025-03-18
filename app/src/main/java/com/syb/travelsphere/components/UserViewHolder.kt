package com.syb.travelsphere.components

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
    private var userNameTextView: TextView? = null
    private var userOriginTextView: TextView? = null
    private var userProfilePictureImageView: ImageView? = null

    init {
        userNameTextView = binding.userName
        userOriginTextView = binding.userOrigin
        userProfilePictureImageView = binding.userProfilePicture
    }

    fun bind(user: User) {
        this.user = user

        // Set the username and phoneNumber
        userNameTextView?.text = user.userName
        userOriginTextView?.text = user.phoneNumber

        if (!user.profilePictureUrl.isNullOrEmpty()) {
            try {
                val userProfilePictureUrl = user.profilePictureUrl
                if (userProfilePictureUrl != null) {
                    userProfilePictureImageView?.let { imageView ->
                        Model.shared.getImageByUrl(userProfilePictureUrl) { bitmap ->
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                } else {
                    // If decoding fails, set a default image
                    userProfilePictureImageView?.setImageResource(R.drawable.default_user)
                }
            } catch (e: Exception) {
                // If decoding fails, set a default image
                userProfilePictureImageView?.setImageResource(R.drawable.default_user)
            }
        }

        // Handle item click
        itemView.setOnClickListener {
            this.user?.let { onUserClick(it) }
        }
    }
}