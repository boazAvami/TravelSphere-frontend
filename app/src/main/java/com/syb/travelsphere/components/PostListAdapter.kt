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
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post

class PostListAdapter(private var posts: List<Post>, private val onPostClick: (Post) -> Unit) : RecyclerView.Adapter<PostListAdapter.PostViewHolder>() {

    private lateinit var authManager: AuthManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_list_item, parent, false)
        authManager = AuthManager()

        return PostViewHolder(view)
    }

//    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
//        val post = posts[position]
//        holder.bind(post)
//    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        if (posts.isEmpty() || position >= posts.size) {
            Log.e(TAG, "Attempted to access invalid index: $position")
            return
        }
        holder.bind(posts[position])
    }


    override fun getItemCount() = posts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postPhoto: ImageView = itemView.findViewById(R.id.postPhoto)
        private val postLocation: TextView = itemView.findViewById(R.id.postLocation)
        private val postDescription: TextView = itemView.findViewById(R.id.postDescription)
//        private val postLikes: TextView = itemView.findViewById(R.id.postLikes)
        private val postUserName: TextView = itemView.findViewById(R.id.postUserName)

        fun bind(post: Post) {
            // Check if the base64 string is not null or empty
            val base64Image = post.photos[0]
            if (!base64Image.isNullOrEmpty()) {
                try {
                    val bitmap = decodeBase64Image(base64Image)
                    postPhoto.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    // Handle the error by logging it or showing a default image
                    postPhoto.setImageResource(R.drawable.default_post)  // Set a default image if decoding fails
                }
            } else {
                postPhoto.setImageResource(R.drawable.default_post)  // Set a default image if there's no base64 string
            }

            // Set the location, description
            authManager.getCurrentUser()?.let { currentUser ->
                val currentUserId = currentUser.uid
                Model.shared.getUserById(currentUserId) { currentUserData ->
                    if (currentUserData != null) {
                        postUserName.text = currentUserData.userName
                    }
                }
            }
//            postUserName.text = post.username
            postLocation.text = post.locationName
            postDescription.text = post.description
//            postLikes.text = "Likes: ${post.likes}"

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

    companion object {
        private const val TAG = "PostListAdapter"
    }
}
