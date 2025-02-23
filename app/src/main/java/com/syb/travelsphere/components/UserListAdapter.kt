package com.syb.travelsphere.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.R
import com.syb.travelsphere.model.User

class UserListAdapter(private val users: List<User>, private val onUserClick: (User) -> Unit) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userProfilePicture: ImageView = itemView.findViewById(R.id.userProfilePicture)
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val userOrigin: TextView = itemView.findViewById(R.id.userOrigin)

        fun bind(user: User) {
            // Set the username and origin
            userName.text = user.userName
//            userOrigin.text = user.originCountry // TODO: remove maybe

            // Load the profile picture from base64
            val base64Image = user.profilePictureUrl // TODO: change the code commented below to get a picture from saved url if not empty
//            if (!base64Image.isNullOrEmpty()) {
//                try {
//                    val bitmap = decodeBase64Image(base64Image)
//                    userProfilePicture.setImageBitmap(bitmap)
//                } catch (e: Exception) {
//                    // If decoding fails, set a default image
//                    userProfilePicture.setImageResource(R.drawable.default_user)
//                }
//            } else {
//                // Set default image if no profile picture is provided
//                userProfilePicture.setImageResource(R.drawable.default_user)
//            }

            // Handle item click
            itemView.setOnClickListener {
                onUserClick(user)  // Pass the user data back to the calling function
            }
        }

        // Helper function to decode base64 string to Bitmap
        private fun decodeBase64Image(base64String: String): Bitmap {
            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
    }
}
