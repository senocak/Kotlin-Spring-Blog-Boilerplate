package com.github.senocak.service

import com.github.senocak.controller.CategoryController
import com.github.senocak.controller.UserController
import com.github.senocak.domain.User
import com.github.senocak.domain.Role
import com.github.senocak.domain.Post
import com.github.senocak.domain.Comment
import com.github.senocak.domain.Category
import com.github.senocak.domain.dto.auth.RoleResponse
import com.github.senocak.domain.dto.category.CategoryDto
import com.github.senocak.domain.dto.comment.CommentDto
import com.github.senocak.domain.dto.post.PostDto
import com.github.senocak.domain.dto.user.UserResponse
import java.util.Date
import java.util.stream.Collectors

object DtoConverter {
    /**
     * @param category -- Category object to convert to dto object
     * @return -- CategoryDto object
     */
    fun convertEntityToDto(category: Category): CategoryDto {
        val categoryDto = CategoryDto()
        categoryDto.resourceId = category.id
        categoryDto.slug = category.slug
        categoryDto.name = category.name
        categoryDto.image = CategoryController.URL + "/" + category.slug + "/image"
        categoryDto.resourceUrl = CategoryController.URL + "/" + category.slug
        if (category.posts != null && category.posts!!.size > 0) {
            categoryDto.postDto =
                category.posts!!.stream()
                    .map{ p ->
                        p.categories = null
                        convertPostEntityToPostsDto(p)
                    }
                    .collect(Collectors.toList())

        }
        return categoryDto
    }

    /**
     * @param post -- Post object to convert to dto object
     * @return -- PostDto object
     */
    fun convertPostEntityToPostsDto(post: Post): PostDto {
        val postsDto = PostDto()
        postsDto.resourceId = post.id
        postsDto.title = post.title
        postsDto.slug = post.slug
        postsDto.body = post.body
        if (post.categories != null && post.categories!!.size > 0) {
            postsDto.categories = post.categories!!.stream()
                    .map { c ->
                        c.posts = null
                        convertEntityToDto(c)
                    }
                    .collect(Collectors.toList())
        }
        if (post.comments != null && post.comments!!.isNotEmpty()) {
            postsDto.comments =
                post.comments!!.stream()
                    .filter(Comment::approved)
                    .map(DtoConverter::convertEntityToDto)
                    .collect(Collectors.toList())
        }
        var tags: List<String>? = ArrayList()
        if (post.tags != null && post.tags!!.isNotEmpty())
            tags = post.tags!!
        postsDto.tags = tags
        postsDto.user = convertEntityToDto(post.user!!)
        postsDto.createdAt = convertDateToLong(post.createdAt)
        postsDto.updatedAt = convertDateToLong(post.updatedAt)
        return postsDto
    }

    /**
     * @param comment -- Comment object to convert to dto object
     * @return -- CommentDto object
     */
    fun convertEntityToDto(comment: Comment): CommentDto {
        val commentDto = CommentDto()
        commentDto.resourceId = comment.id
        commentDto.name = comment.name
        commentDto.email = comment.email
        commentDto.body = comment.body
        commentDto.createdAt = convertDateToLong(comment.createdAt)
        commentDto.approved = comment.approved
        return commentDto
    }

    /**
     * @param user -- User object to convert to dto object
     * @return -- UserResponse object
     */
    fun convertEntityToDto(user: User): UserResponse {
        return UserResponse(
            user.name, user.email, user.username,
            user.roles.stream().map(DtoConverter::convertEntityToDto).collect(Collectors.toSet()),
            "${UserController.URL}/${user.username}"
        )
    }

    /**
     * @param role -- role object to convert to dto object
     * @return -- RoleResponse object
     */
    fun convertEntityToDto(role: Role): RoleResponse {
        val roleResponse = RoleResponse()
        roleResponse.name = role.name
        return roleResponse
    }

    /**
     * @param date -- Date object to convert to long timestamp
     * @return -- converted timestamp object that is long type
     */
    private fun convertDateToLong(date: Date): Long {
        return date.time / 1000
    }
}