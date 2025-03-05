package com.syb.travelsphere.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.R
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.utils.TimeUtil.formatTimestamp

class PostListAdapter(private val posts: MutableList<Post>, private val onPostClick: (Post) -> Unit) :
    RecyclerView.Adapter<PostListAdapter.PostViewHolder>() {

    companion object {
        private const val TAG = "PostListAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_list_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()  // Clear old data
        posts.addAll(newPosts)  // Add new data
        notifyDataSetChanged()  // Notify RecyclerView of changes
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postPhoto: ImageView = itemView.findViewById(R.id.postPhoto)
        private val postLocation: TextView = itemView.findViewById(R.id.postLocation)
        private val postDescription: TextView = itemView.findViewById(R.id.postDescription)
        private val postUserName: TextView = itemView.findViewById(R.id.postUserName)
        private val postCreatedTime: TextView = itemView.findViewById(R.id.postTimestamp)

        fun bind(post: Post) {
            // Check if the base64 string is not null or empty
//            val base64Image = post.photos[0]
            val firstImageUrl = post.photos.getOrNull(0)
            if (!firstImageUrl.isNullOrEmpty()) {
                try {
                    Model.shared.getImageByUrl(firstImageUrl) { bitmap ->
                        postPhoto.setImageBitmap(bitmap)

                    }
                } catch (e: Exception) {
                    // Handle the error by logging it or showing a default image
                    postPhoto.setImageResource(R.drawable.default_post)  // Set a default image if decoding fails
                }
            } else {
                postPhoto.setImageResource(R.drawable.default_post)  // Set a default image if there's no base64 string
            }

            Model.shared.getUserById(post.ownerId) { user ->
                Log.d(TAG, "bind: user: $user")
                postUserName.text = user.value?.userName ?: "Unknown"
            }

            // Set the location, description
            postLocation.text = post.locationName
            postDescription.text = post.description
            postCreatedTime.text = formatTimestamp(post.creationTime)

            // Handle item click
            itemView.setOnClickListener {
                onPostClick(post)  // Pass the post back to the calling function
            }
        }

        // Helper function to decode base64 string to Bitmap
        private fun decodeBase64Image(base64String: String): Bitmap {
            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
    }
}