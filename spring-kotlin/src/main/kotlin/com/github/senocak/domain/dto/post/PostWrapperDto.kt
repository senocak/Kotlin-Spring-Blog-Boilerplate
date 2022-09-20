package com.github.senocak.domain.dto.post

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema

class PostWrapperDto(
    @JsonProperty("post")
    @Schema(description = "Post wrapper", required = true, name = "post", type = "PostDto")
    var postDto: PostDto? = null
): BaseDto()