package com.github.senocak.factory

import com.github.senocak.TestConstants.COMMENT_BODY
import com.github.senocak.TestConstants.COMMENT_EMAIL
import com.github.senocak.TestConstants.COMMENT_TITLE
import com.github.senocak.domain.Comment
import com.github.senocak.factory.PostFactory.createPost
import java.util.Date

object CommentFactory {

    /**
     * Creates a new Comment instance.
     * @return a new Comment instance.
     */
    fun createComment(): Comment {
        val comment = Comment()
        comment.name = COMMENT_TITLE
        comment.body = COMMENT_BODY
        comment.approved = true
        comment.email = COMMENT_EMAIL
        comment.post = createPost()
        comment.createdAt = Date()
        comment.updatedAt = Date()
        return comment
    }
}
