package com.github.senocak.controller

import com.github.senocak.domain.dto.settings.Logger
import com.github.senocak.util.AppConstants.getLogger
import com.github.senocak.util.AppConstants.setLevel
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class GraphQLController {

    @QueryMapping
    fun getLogLevel(): String {
        return getLogger().level.levelStr
    }

    @MutationMapping
    fun changeLogLevel(@Argument loglevel: String): String {
        val logger = Logger()
        setLevel(loglevel)
        logger.level = loglevel
        return loglevel
    }
}
