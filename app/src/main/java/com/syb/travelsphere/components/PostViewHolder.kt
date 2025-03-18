package com.syb.travelsphere.ui

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.R
import com.syb.travelsphere.databinding.PostListItemBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.utils.TimeUtil.formatTimestamp

class PostViewHolder(
    binding: PostListItemBinding,
    private val postOwnerUsers: Map<String, String>?,
    private val onPostClick: (Post) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private var post: Post? = null
    private var postUserNameTextView: TextView? = null
    private var postLocationTextView: TextView? = null
    private var postPhotoImageView: ImageView? = null
    private var postDescriptionTextView: TextView? = null
    private var postTimestampTextView: TextView? = null

    init {
        postUserNameTextView = binding.postUserName
        postLocationTextView = binding.postLocation
        postPhotoImageView = binding.postPhoto
        postDescriptionTextView = binding.postDescription
        postTimestampTextView = binding.postTimestamp
    }

    fun bind(post: Post) {
        this.post = post

        postUserNameTextView?.text = postOwnerUsers?.get(post.ownerId) ?: "Unknown"

        // Truncate location name if too long
        val maxLocationLength = 20
        postLocationTextView?.text = if (post.locationName.length > maxLocationLength)
            post.locationName.take(maxLocationLength) + "..." else post.locationName

        // Check if the base64 string is not null or empty
        val firstImageUrl = post.photos.getOrNull(0)
        if (!firstImageUrl.isNullOrEmpty()) {
            try {
                postPhotoImageView?.let { imageView ->
                    Model.shared.getImageByUrl(firstImageUrl) { bitmap ->
                        imageView.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                // Handle the error by showing a default image
                postPhotoImageView?.setImageResource(R.drawable.default_post)
            }
        } else {
            postPhotoImageView?.setImageResource(R.drawable.default_post)
        }

        // Set the description and creationTime
        postDescriptionTextView?.text = post.description
        postTimestampTextView?.text = formatTimestamp(post.creationTime)

        // Handle item click
        itemView.setOnClickListener {
            this.post?.let { onPostClick(it) }
        }
    }
}