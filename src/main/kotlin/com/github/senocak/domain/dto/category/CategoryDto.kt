package com.github.senocak.domain.dto.category

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.domain.dto.BaseDto
import com.github.senocak.domain.dto.post.PostDto
import io.swagger.v3.oas.annotations.media.Schema

@JsonPropertyOrder("resourceId", "name", "slug", "image", "posts", "resourceUrl")
class CategoryDto : BaseDto() {
    @Schema(example = "1cb9374e-4e52-4142-a1af-16144ef4a271", description = "resourceId of the category", required = true, name = "resourceId", type = "String")
    var resourceId: String? = null

    @Schema(example = "springboot", description = "Name of the category", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "springboot", description = "Slug of the category", required = true, name = "slug", type = "String")
    var slug: String? = null

    @Schema(example = "springboot", description = "Image of the category", required = true, name = "image", type = "String")
    var image: String? = null

    @Schema(example = "/api/v1/categories/springboot", description = "ResourceUrl of the category", required = true, name = "resourceUrl", type = "String")
    var resourceUrl: String? = null

    @JsonProperty("posts")
    @Schema(description = "Name of the category", required = true, name = "postDto")
    var postDto: List<PostDto>? = null
}