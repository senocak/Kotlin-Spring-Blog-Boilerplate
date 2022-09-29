package com.github.senocak

import com.github.senocak.util.AppConstants
import com.github.senocak.util.AppConstants.getLogger
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener

@SpringBootApplication
class BlogApplication

fun main(args: Array<String>) {
    SpringApplicationBuilder(BlogApplication::class.java)
        .bannerMode(Banner.Mode.CONSOLE)
        .logStartupInfo(true)
        .listeners(ApplicationListener {
            event: ApplicationEvent -> getLogger().debug("#### event> " + event.javaClass.canonicalName)
        })
        .build()
        .run(*args)
    AppConstants.setLevel("info")
}
