package com.github.senocak.domain.dto.user

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema

@JsonPropertyOrder("user", "token", "refreshToken")
class UserWrapperResponse(
    @JsonProperty("user")
    @Schema(required = true)
    var userResponse: UserResponse,

    @Schema(example = "eyJraWQiOiJ...", description = "Jwt Token", required = true, name = "token", type = "String")
    var token: String? = null
): BaseDto()