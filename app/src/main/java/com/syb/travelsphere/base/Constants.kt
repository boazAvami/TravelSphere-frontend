package com.syb.travelsphere.base

import com.syb.travelsphere.model.User

typealias UsersCallback = (List<User>) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {

    object COLLECTIONS {
        const val USERS = "users"
    }
}