package com.github.senocak.domain.dto.settings

import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema

class Logger(
    @Schema(example = "debug", description = "Level of the log", required = true, name = "level", type = "String")
    var level: String? = null
): BaseDto()