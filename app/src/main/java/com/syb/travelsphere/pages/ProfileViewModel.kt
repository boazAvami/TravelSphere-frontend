package com.syb.travelsphere.pages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.syb.travelsphere.base.PostsCallback
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post

class ProfileViewModel : ViewModel() {

//    val userPosts: LiveData<List<Post>> = Model.shared.posts

    private val _userPosts = MutableLiveData<List<Post>>() // Private mutable LiveData
    val userPosts: LiveData<List<Post>> get() = _userPosts // Public immutable LiveData

//    private val _userPosts = MutableLiveData<List<Post>>()
//    val userPosts: LiveData<List<Post>> get() = _userPosts

//    fun loadUserPosts(userId: String) {
//        _userPosts.value = Model.shared.getPostsByUserId(userId))
//    }

    fun refreshUserPosts(ownerId: String) {
        Model.shared.getPostsByUserId(ownerId) { posts ->
            Log.d("profileViewModel", "refreshUserPosts: ${posts.size}")
            _userPosts.postValue(posts)
        }
    }
}

