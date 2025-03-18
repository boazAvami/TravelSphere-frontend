package com.syb.travelsphere.pages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.syb.travelsphere.base.PostsCallback
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post

class ProfileViewModel : ViewModel() {
    private val _userPosts = MutableLiveData<List<Post>>() // Private mutable LiveData
    val userPosts: LiveData<List<Post>> get() = _userPosts // Public immutable LiveData

    private val _postModificationEvent = MutableLiveData<Long>()
    val postModificationEvent: LiveData<Long> = _postModificationEvent

    fun refreshUserPosts(ownerId: String) {
        Model.shared.getPostsByUserId(ownerId) { posts ->
            Log.d("profileViewModel", "refreshUserPosts: ${posts.size}")
            _userPosts.postValue(posts)
        }
    }

    // Call this when a post is modified
    fun notifyPostModified() {
        _postModificationEvent.value = System.currentTimeMillis()
    }
}

