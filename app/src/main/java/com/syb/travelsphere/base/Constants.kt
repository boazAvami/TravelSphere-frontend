package com.syb.travelsphere.base

import com.syb.travelsphere.model.Post
import com.syb.travelsphere.model.User

typealias UsersCallback = (List<User>) -> Unit
typealias PostsCallback = (List<Post>) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {

    object COLLECTIONS {
        const val USERS = "users"
    }
}