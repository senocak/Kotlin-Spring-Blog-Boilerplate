package com.github.senocak.domain.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class LoginRequest(
    @JsonProperty("username")
    @Schema(example = "asenocak", description = "Username of the user", required = true, name = "username", type = "String")
    @field:NotBlank
    @field:Size(min = 3, max = 50)
    var username: String? = null,

    @Schema(description = "Password of the user", name = "password", type = "String", example = "password", required = true)
    @field:NotBlank
    @field:Size(min = 6, max = 20)
    var password: String? = null
): BaseDto()