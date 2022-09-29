package com.github.senocak.event

import org.springframework.context.ApplicationEvent
import java.time.Instant
import java.util.Date

class OnUserLogoutSuccessEvent(
    val username: String,
    val token: String,
    val type: String,
    val expireTimeStamp: Long
) : ApplicationEvent(username) {
    val eventTime: Date = Date.from(Instant.now())
}
