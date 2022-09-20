package com.github.senocak.domain.dto.user

import com.github.senocak.domain.dto.BaseDto
import com.github.senocak.util.validation.PasswordMatches
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Size

@PasswordMatches
data class UpdateUserDto(
    @Schema(example = "Anil", description = "Name", required = true, name = "name", type = "String")
    @field:Size(min = 4, max = 40)
    var name: String? = null,

    @Schema(example = "Anil123", description = "Password", required = true, name = "password", type = "String")
    @field:Size(min = 6, max = 20)
    var password: String? = null,

    @Schema(example = "Anil123", description = "Password confirmation", required = true, name = "password", type = "String")
    @field:Size(min = 6, max = 20)
    var password_confirmation: String? = null
): BaseDto()