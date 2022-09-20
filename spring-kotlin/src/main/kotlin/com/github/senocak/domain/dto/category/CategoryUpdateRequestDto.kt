package com.github.senocak.domain.dto.category

import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Size

data class CategoryUpdateRequestDto(
    @Schema(example = "springboot", description = "Name of the category", required = true, name = "name", type = "String")
    @field:Size(min = 3, max = 30)
    var name: String? = null,

    @Schema(example = "springboot", description = "Image of the category", required = true, name = "image", type = "String")
    var image: String? = null
): BaseDto()