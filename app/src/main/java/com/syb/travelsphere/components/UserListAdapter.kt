package com.syb.travelsphere.components

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.databinding.ItemUserBinding
import com.syb.travelsphere.model.User

class UserListAdapter(
    private var users: List<User>?,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserViewHolder>() {

    companion object {
        private const val TAG = "UserListAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users?.get(position)
        Log.d(TAG, "Binding user at position $position: ${user?.userName}")
        if (user != null) {
            holder.bind(user)
        }
    }

    override fun getItemCount() = users?.size ?: 0

    fun update(users: List<User>?) {
        if (this.users == users) return // Prevent unnecessary UI updates

        Log.d(TAG, "Updating users: Old size = ${this.users?.size}, New size = ${users?.size}")

        this.users = users ?: emptyList()
        notifyDataSetChanged() // Update UI
    }
}