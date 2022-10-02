package com.github.senocak.domain.dto.auth

import org.springframework.context.ApplicationEvent
import java.time.Instant
import java.util.Date

class OnUserLogout(
    val username: String,
    val token: String,
    val type: String,
    val expireTimeStamp: Long
) : ApplicationEvent(username) {
    val eventTime: Date = Date.from(Instant.now())
}
