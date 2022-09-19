package com.github.senocak.domain.dto.auth

import com.github.senocak.domain.dto.BaseDto
import com.github.senocak.util.RoleName
import io.swagger.v3.oas.annotations.media.Schema

class RoleResponse : BaseDto() {
    @Schema(example = "ROLE_USER", description = "Name of the role", required = true, name = "name")
    var name: RoleName? = null
}