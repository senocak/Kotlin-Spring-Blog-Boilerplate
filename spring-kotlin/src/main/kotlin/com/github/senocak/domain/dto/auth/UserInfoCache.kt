package com.github.senocak.domain.dto.auth

import org.springframework.context.ApplicationEvent

class UserInfoCache(
    val username: String,
    val token: String,
    val type: String,
    val expireTimeStamp: Long
) : ApplicationEvent(username)