package com.github.senocak.domain.dto.comment

import com.github.senocak.domain.dto.BaseDto
import com.github.senocak.util.AppConstants
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class CommentRequestDto(
    @Schema(example = "anil", description = "Name of the comment", required = true, name = "name", type = "String")
    @field:NotBlank
    @field:Size(min = 3, max = 30)
    var name: String? = null,

    @Schema(example = "anil@senocak.com", description = "Email of the comment", required = true, name = "email", type = "String")
    @field:Size(max = 30)
    @field:Pattern(regexp = AppConstants.MAIL_REGEX)
    var email: String? = null,

    @Schema(example = "body 's here", description = "Body of the comment", required = true, name = "body", type = "String")
    @field:NotBlank
    @field:Size(min = 3, max = 250)
    var body: String? = null
): BaseDto()