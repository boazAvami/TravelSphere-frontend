package com.syb.travelsphere.pages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post

class AllPostsViewModel : ViewModel() {

    val posts: LiveData<List<Post>> = Model.shared.posts
    private val _postOwnerUsers = MutableLiveData<Map<String, String>>() // Stores userId -> username
    val postOwnerUsers: LiveData<Map<String, String>> get() = _postOwnerUsers

    fun refreshPosts() {
        Model.shared.refreshAllPosts()
    }

     fun fetchPostOwnerUsers(posts: List<Post>, callback: (Map<String, String>) -> Unit) {
        val ownerIds = posts.map { it.ownerId }.distinct() // Extract unique owner IDs
        if (ownerIds.isEmpty()) {
            _postOwnerUsers.postValue(emptyMap()) // If no owners, return empty map
            return
        }

        Model.shared.fetchUsersByIds(ownerIds) { users ->
            val userMap = users.associate { it.id to it.userName }
            _postOwnerUsers.postValue(userMap) // Update UI once all usernames are ready
            callback(userMap)
        }
    }
}

