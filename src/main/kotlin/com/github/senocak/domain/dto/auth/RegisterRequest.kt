package com.github.senocak.domain.dto.auth

import com.github.senocak.domain.dto.BaseDto
import com.github.senocak.util.validation.ValidEmail
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class RegisterRequest(
    @Schema(example = "Lorem Ipsum", description = "Name of the user", required = true, name = "name", type = "String")
    @field:NotBlank
    @field:Size(min = 4, max = 40)
    var name: String? = null,

    @Schema(example = "asenocak", description = "Username of the user", required = true, name = "username", type = "String")
    @field:NotBlank
    @field:Size(min = 3, max = 15)
    var username: String? = null,

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
//    @field:NotBlank @Size(max = 30)
//    @field:Pattern(regexp = AppConstants.MAIL_REGEX)
    @field:ValidEmail
    var email: String? = null,

    @Schema(example = "asenocak123", description = "Password of the user", required = true, name = "password", type = "String")
    @field:NotBlank
    @field:Size(min = 6, max = 20)
    var password: String? = null
): BaseDto()