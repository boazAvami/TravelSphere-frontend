package com.syb.travelsphere.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.databinding.PostListItemBinding
import com.syb.travelsphere.model.Post

class PostListAdapter(
    private var posts: List<Post>?,
    private var postOwnerUsers: Map<String, String>?,
    private val onPostClick: (Post) -> Unit
) : RecyclerView.Adapter<PostViewHolder>() {

    companion object {
        private const val TAG = "PostListAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding, postOwnerUsers, onPostClick)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts?.get(position)
        if (post != null) {
            holder.bind(post)
        }
    }

    override fun getItemCount() = posts?.size ?: 0

    fun update(posts: List<Post>?, postOwnerUsers: Map<String, String>) {
        this.posts = posts
        this.postOwnerUsers = postOwnerUsers
        notifyDataSetChanged()
    }
}