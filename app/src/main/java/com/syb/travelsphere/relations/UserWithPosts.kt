package com.syb.travelsphere.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.model.User

data class UserWithPosts(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "ownerId"
    )
    val posts: List<Post>
)
