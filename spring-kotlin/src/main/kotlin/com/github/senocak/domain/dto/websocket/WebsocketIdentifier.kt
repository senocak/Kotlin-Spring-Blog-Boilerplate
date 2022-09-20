package com.github.senocak.domain.dto.websocket

import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.socket.WebSocketSession

data class WebsocketIdentifier(
    @Schema(example = "user", description = "user", required = true, name = "username", type = "String")
    var user: String? = null,

    @Schema(description = "channelId", name = "channelId", type = "String", example = "channelId", required = true)
    var channelId: String? = null,

    @Schema(description = "token", name = "token", type = "String", example = "token", required = true)
    var token: String? = null,

    @Schema(description = "session", name = "session", type = "String", example = "session", required = true)
    var session: WebSocketSession? = null
): BaseDto()