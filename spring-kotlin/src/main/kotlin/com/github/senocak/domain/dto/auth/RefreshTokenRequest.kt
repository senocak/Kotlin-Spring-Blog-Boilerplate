package com.github.senocak.domain.dto.auth

import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class RefreshTokenRequest(
    @Schema(example = "Lorem Ipsum", description = "Name of the user", required = true, name = "name", type = "String")
    @field:NotBlank
    @field:Size(min = 4, max = 40)
    var token: String? = null
): BaseDto()