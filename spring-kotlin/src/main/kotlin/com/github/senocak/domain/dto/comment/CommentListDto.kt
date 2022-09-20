package com.github.senocak.domain.dto.comment

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@JsonPropertyOrder("comment", "next", "total", "resourceUrl")
class CommentListDto(
    @JsonProperty("comment")
    @ArraySchema(schema = Schema(description = "comment", required = true, type = "CommentDto"))
    var commentDtoList: List<CommentDto>? = null,

    @Schema(example = "1", description = "Total page", required = true, name = "total", type = "Long")
    var total: Long = 0,

    @Schema(example = "0", description = "Next page", required = true, name = "next", type = "Long")
    var next: Long = 0,

    @Schema(example = "/api/v1/comments/springboot", description = "ResourceUrl of the comment", required = true, name = "resourceUrl", type = "String")
    var resourceUrl: String? = null
): BaseDto()