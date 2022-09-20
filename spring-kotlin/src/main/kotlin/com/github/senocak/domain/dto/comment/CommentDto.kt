package com.github.senocak.domain.dto.comment

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema

@JsonPropertyOrder("resourceId", "name", "email", "body", "approved", "createdAt")
class CommentDto : BaseDto() {
    @Schema(example = "1cb9374e-4e52-4142-a1af-16144ef4a271", description = "resourceId of the comment", required = true, name = "resourceId", type = "String")
    var resourceId: String? = null

    @Schema(example = "Anil", description = "Name of the comment", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "spring@boot.com", description = "Email of the category", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(example = "example body", description = "Name of the category", required = true, name = "body", type = "String")
    var body: String? = null

    @Schema(example = "false", description = "Is comment approved?", required = true, name = "approved", type = "Boolean")
    var approved: Boolean = false

    @Schema(example = "2022-06-24 13:25:24", description = "Date of the creation", required = true, name = "createdAt", type = "Long")
    var createdAt: Long? = null
}