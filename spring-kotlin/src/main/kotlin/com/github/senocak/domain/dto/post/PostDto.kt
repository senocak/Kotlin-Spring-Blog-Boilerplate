package com.github.senocak.domain.dto.post

import com.github.senocak.domain.dto.BaseDto
import com.github.senocak.domain.dto.category.CategoryDto
import com.github.senocak.domain.dto.comment.CommentDto
import com.github.senocak.domain.dto.user.UserResponse
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@JsonPropertyOrder(
    "resourceId",
    "slug",
    "title",
    "body",
    "image",
    "user",
    "categories",
    "comments",
    "tags",
    "createdAt",
    "updatedAt",
    "resourceUrl"
)
class PostDto : BaseDto() {
    @Schema(example = "1cb9374e-4e52-4142-a1af-16144ef4a271", description = "resourceId of the post", required = true, name = "resourceId", type = "String")
    var resourceId: String? = null

    @Schema(example = "What is Spring?", description = "Name of the post", required = true, name = "title", type = "String")
    var title: String? = null

    @Schema(example = "what-is-spring", description = "Slug of the post", required = true, name = "slug", type = "String")
    var slug: String? = null

    @Schema(example = "Spring is a framework", description = "Body of the category", required = true, name = "body", type = "String")
    var body: String? = null

    @Schema(description = "User detail of post", required = true, name = "user", type = "UserResponse")
    var user: UserResponse? = null

    @ArraySchema(schema = Schema(description = "Categories of the post", required = true, type = "CategoryDto"))
    var categories: List<CategoryDto>? = null

    @ArraySchema(schema = Schema(description = "Comments of the post", required = true, type = "CommentDto"))
    var comments: List<CommentDto>? = null

    @ArraySchema(schema = Schema(example = "java", description = "Tags of the post", required = true, name = "tags", type = "CommentDto"))
    var tags: List<String>? = null

    @Schema(example = "2022-06-24 13:25:24", description = "Date of the creation", required = true, name = "createdAt", type = "Long")
    var createdAt: Long? = null

    @Schema(example = "2022-06-24 13:25:24", description = "Date of the update", required = true, name = "createdAt", type = "Long")
    var updatedAt: Long? = null

    @Schema(example = "/api/v1/posts/springboot", description = "ResourceUrl of the post", required = true, name = "resourceUrl", type = "String")
    var resourceUrl: String? = null
}