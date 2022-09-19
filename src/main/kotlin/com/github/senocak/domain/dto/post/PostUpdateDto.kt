package com.github.senocak.domain.dto.post

import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Size

data class PostUpdateDto(
    @Schema(example = "What is spring boot?", description = "Name of the post", required = true, name = "title", type = "String")
    @field:Size(min = 4, max = 40)
    var title: String? = null,

    @Schema(example = "Spring Boot is a project that is built on the top of the Spring Framework", description = "Body of the post", required = true, name = "body", type = "String")
    @field:Size(min = 3)
    var body: String? = null,

    @ArraySchema(schema = Schema(example = "springboot", description = "Name of the category", required = true, name = "category", type = "String"))
    var category: List<String>? = null,

    @ArraySchema(schema = Schema(example = "java", description = "Name of the tag", required = true, name = "tags", type = "String"))
    var tags: List<String>? = null
): BaseDto()