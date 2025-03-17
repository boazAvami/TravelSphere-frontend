package com.syb.travelsphere.components

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.R
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User

class UserListAdapter(private var users: List<User>?, private val onUserClick: (User) -> Unit) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users?.get(position)
        Log.d("UserListAdapter", "Binding user at position $position: ${user?.userName}")
        if (user != null) {
            holder.bind(user)
        }
    }

    override fun getItemCount() = users?.size ?: 0

    fun update(users: List<User>?) {
        if (this.users == users) return // Prevent unnecessary UI updates

        Log.d("UserListAdapter", "Updating users: Old size = ${this.users?.size}, New size = ${users?.size}")

        this.users = users ?: emptyList()
        notifyDataSetChanged()

        this.users = users ?: emptyList()
        notifyDataSetChanged() // Update UI
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userProfilePicture: ImageView = itemView.findViewById(R.id.userProfilePicture)
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val userPhoneNumber: TextView = itemView.findViewById(R.id.userOrigin)

        fun bind(user: User) {
            // Set the username and phoneNumber
            userName.text = user.userName
            userPhoneNumber.text = user.phoneNumber

            if (!user.profilePictureUrl.isNullOrEmpty()) {
                try {
                    val userProfilePictureUrl = user.profilePictureUrl
                    if (userProfilePictureUrl != null) {
                        Model.shared.getImageByUrl(userProfilePictureUrl) { bitmap ->
                            userProfilePicture.setImageBitmap(bitmap)
                        }
                    } else {
                        // If decoding fails, set a default image
                        userProfilePicture.setImageResource(R.drawable.default_user)
                    }
                } catch (e: Exception) {
                    // If decoding fails, set a default image
                    userProfilePicture.setImageResource(R.drawable.default_user)
                }
            }

            // Handle item click
            itemView.setOnClickListener {
                onUserClick(user)  // Pass the user data back to the calling function
            }
        }
    }
}
