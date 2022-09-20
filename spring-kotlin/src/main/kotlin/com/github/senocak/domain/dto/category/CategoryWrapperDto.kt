package com.github.senocak.domain.dto.category

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema

class CategoryWrapperDto(
    @JsonProperty("category")
    @Schema(description = "Category wrapper", required = true, name = "category", type = "CategoryDto")
    var categoryDto: CategoryDto? = null
) : BaseDto()