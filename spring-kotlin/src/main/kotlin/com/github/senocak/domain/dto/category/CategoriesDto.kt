package com.github.senocak.domain.dto.category

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@JsonPropertyOrder("category", "next", "total", "resourceUrl")
class CategoriesDto(
    @JsonProperty("category")
    @ArraySchema(schema = Schema(description = "category", required = true, type = "CategoryDto"))
    var categoryDtoList: List<CategoryDto>? = null,

    @Schema(example = "1", description = "Total page", required = true, name = "total", type = "Long")
    var total: Long = 0,

    @Schema(example = "0", description = "Next page", required = true, name = "next", type = "Long")
    var next: Long = 0,

    @Schema(example = "/api/v1/categories/springboot", description = "ResourceUrl of the category", required = true, name = "resourceUrl", type = "String")
    var resourceUrl: String? = null
)
