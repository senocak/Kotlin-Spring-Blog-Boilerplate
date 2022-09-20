package com.github.senocak.domain.dto.post

import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class CreatePostDto(
    @Schema(example = "What is spring?", description = "Title of the post", required = true, name = "title", type = "String")
    @field:NotBlank
    @field:Size(min = 4, max = 100)
    var title: String? = null,

    @Schema(example = "Spring is a framework", description = "Body of the post", required = true, name = "body", type = "String")
    @field:NotBlank
    @field:Size(min = 3)
    var body: String? = null,

    @ArraySchema(schema = Schema(example = "springboot", description = "Name of the category", required = true, type = "String"))
    @field:Size(min = 1)
    var category: List<String>? = null,

    @ArraySchema(schema = Schema(example = "java", description = "Name of the tag", required = true, type = "String"))
    var tags: List<String>? = null
): BaseDto()
