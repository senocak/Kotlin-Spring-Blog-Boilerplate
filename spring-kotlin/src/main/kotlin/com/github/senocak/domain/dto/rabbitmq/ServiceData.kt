package com.github.senocak.domain.dto.rabbitmq

import com.github.senocak.util.Action

data class ServiceData(
    var action: Action? = null,
    var message: String? = null
)
