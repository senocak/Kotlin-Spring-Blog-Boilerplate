package com.github.senocak.domain.dto.websocket

import com.github.senocak.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.socket.WebSocketSession

data class WsRequestBody(
    var from: String? = null,
    var to: String? = null,
    var content: String? = null,
    var type: String? = null,
    var date: Long? = null
): BaseDto()