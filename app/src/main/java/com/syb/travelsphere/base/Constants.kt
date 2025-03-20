package com.syb.travelsphere.base

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseUser
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.model.User

typealias UsersCallback = (List<User>) -> Unit
typealias UserCallback = (User?) -> Unit
typealias PostsCallback = (List<Post>) -> Unit
typealias PostCallback = (Post?) -> Unit
typealias EmptyCallback = () -> Unit
typealias AuthCallback = (FirebaseUser?) -> Unit
typealias ImageCallback = (String?) -> Unit
typealias BooleanCallback = (Boolean) -> Unit
typealias BitmapCallback = (Bitmap?) -> Unit

object Constants {

    object COLLECTIONS {
        const val USERS = "users"
        const val POSTS = "posts"
        const val DELETED_POSTS = "deleted_posts"

    }
}