package com.syb.travelsphere.components

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.databinding.ItemUserBinding
import com.syb.travelsphere.model.User

class UserListAdapter(
    private var users: List<User> = emptyList(),
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserViewHolder>() {

    companion object {
        private const val TAG = "UserListAdapter"
    }

    init {
        // Enable stable IDs for better RecyclerView performance
        setHasStableIds(true)
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
        val user = users.getOrNull(position)
        Log.d(TAG, "Binding user at position $position: ${user?.userName}")
        if (user != null) {
            holder.bind(user)
        }
    }

    override fun getItemCount() = users.size

    // Important for stable item identification
    override fun getItemId(position: Int): Long {
        return users.getOrNull(position)?.id?.hashCode()?.toLong() ?: position.toLong()
    }

    fun update(newUsers: List<User>?) {
        if (this.users == newUsers) return // Prevent unnecessary UI updates

        Log.d(TAG, "Updating users: Old size = ${this.users.size}, New size = ${newUsers?.size}")

        val oldUsers = this.users
        this.users = newUsers ?: emptyList()

        // Use DiffUtil to calculate and dispatch efficient updates
        val diffCallback = UserDiffCallback(oldUsers, this.users)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
    }

    private class UserDiffCallback(
        private val oldUsers: List<User>,
        private val newUsers: List<User>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldUsers.size

        override fun getNewListSize() = newUsers.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldUsers[oldItemPosition].id == newUsers[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldUser = oldUsers[oldItemPosition]
            val newUser = newUsers[newItemPosition]

            // Consider all relevant fields for comparison
            return oldUser.id == newUser.id &&
                    oldUser.userName == newUser.userName &&
                    oldUser.profilePictureUrl == newUser.profilePictureUrl &&
                    oldUser.phoneNumber == newUser.phoneNumber
        }
    }
}