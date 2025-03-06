package com.syb.travelsphere.pages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post

class AllPostsViewModel : ViewModel() {

    val posts: LiveData<List<Post>> = Model.shared.posts

    fun refreshPosts() {
        Model.shared.refreshAllPosts()
    }
}
