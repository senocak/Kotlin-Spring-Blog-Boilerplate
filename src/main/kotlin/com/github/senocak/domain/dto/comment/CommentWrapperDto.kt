package com.github.senocak.domain.dto.comment

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema

class CommentWrapperDto(
    @JsonProperty("comment")
    @Schema(description = "Comment wrapper", required = true, name = "comment", type = "CommentDto")
    var commentDto: CommentDto? = null
): BaseDto()
