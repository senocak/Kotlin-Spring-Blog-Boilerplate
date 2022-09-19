package com.github.senocak.domain.dto.user

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.domain.dto.BaseDto
import com.github.senocak.domain.dto.auth.RoleResponse
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@JsonPropertyOrder("name", "username", "email", "roles", "resourceUrl")
class UserResponse(
    @JsonProperty("name")
    @Schema(example = "Lorem Ipsum", description = "Name of the user", required = true, name = "name", type = "String")
    var name: String,

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String,

    @Schema(example = "asenocak", description = "Username of the user", required = true, name = "username", type = "String")
    var username: String,

    @ArraySchema(schema = Schema(example = "ROLE_USER", description = "Roles of the user", required = true, name = "roles"))
    var roles: Set<RoleResponse>,

    @Schema(example = "/api/v1/user/asenocakUser", description = "Username of the user", required = true, name = "resourceUrl", type = "String")
    var resourceUrl: String
): BaseDto()