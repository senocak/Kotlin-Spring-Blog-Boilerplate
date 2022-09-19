package com.github.senocak.domain.dto.post

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@JsonPropertyOrder("post", "next", "total", "resourceUrl")
class PostsDto(
    @JsonProperty("post")
    @ArraySchema(schema = Schema(description = "post", required = true, type = "PostDto"))
    var postDto: List<PostDto>,

    @Schema(example = "1", description = "Total page", required = true, name = "total", type = "Long")
    var total: Long = 0,

    @Schema(example = "0", description = "Next page", required = true, name = "next", type = "Long")
    var next: Long = 0,

    @Schema(example = "/api/v1/posts/springboot", description = "ResourceUrl of the post", required = true, name = "resourceUrl", type = "String")
    var resourceUrl: String
): BaseDto()