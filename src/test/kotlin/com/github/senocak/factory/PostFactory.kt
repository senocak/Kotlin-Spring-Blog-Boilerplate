package com.github.senocak.factory

import com.github.senocak.TestConstants.POST_BODY
import com.github.senocak.TestConstants.POST_TITLE
import com.github.senocak.domain.Post
import com.github.senocak.factory.UserFactory.createUser
import java.util.Date
import java.util.UUID

object PostFactory {

    /**
     * Creates a new post with random data.
     * @return a new post with random data.
     */
    fun createPost(): Post {
        val post = Post()
        post.id = UUID.randomUUID().toString()
        post.title = POST_TITLE
        post.slug = POST_TITLE
        post.body = POST_BODY
        post.user = createUser()
        post.comments = ArrayList()
        post.tags = ArrayList()
        post.categories = ArrayList()
        post.createdAt = Date()
        post.updatedAt = Date()
        return post
    }
}
