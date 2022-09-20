package com.github.senocak.util

import ch.qos.logback.classic.Level
import com.github.senocak.domain.dto.websocket.WebsocketIdentifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.Normalizer
import java.util.Objects
import java.util.regex.Pattern

object AppConstants {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    val corePoolSize = Runtime.getRuntime().availableProcessors()
    const val DEFAULT_PAGE_NUMBER = "0"
    const val DEFAULT_PAGE_SIZE = "10"
    const val MAIL_REGEX = "^\\S+@\\S+\\.\\S+$"
    const val TOKEN_HEADER_NAME = "Authorization"
    const val TOKEN_PREFIX = "Bearer "
    const val ADMIN = "ADMIN"
    const val USER = "USER"
    const val securitySchemeName = "bearerAuth"
    const val CACHE_CATEGORY = "category"

    /**
     * @param input -- string variable to make it sluggable
     * @return -- sluggable string variable
     */
    fun toSlug(input: String): String {
        val nonLatin: Pattern = Pattern.compile("[^\\w-]")
        val whiteSpace: Pattern = Pattern.compile("[\\s]")
        val noWhiteSpace: String = whiteSpace.matcher(input).replaceAll("-")
        val normalized: String = Normalizer.normalize(noWhiteSpace, Normalizer.Form.NFD)
        return nonLatin.matcher(normalized).replaceAll("")
    }

    fun getWebsocketIdentifier(path: String): WebsocketIdentifier? {
        if (path.isEmpty()) return null
        val fields = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (fields.isEmpty())
            null
        else{
            val websocketIdentifier = WebsocketIdentifier()
            try {
                val user = fields[2]
                websocketIdentifier.user = user
                websocketIdentifier.channelId = fields[3]
            } catch (e: IndexOutOfBoundsException) {
                log.error("Cannot find user or channel id from the path!", e)
            }
            websocketIdentifier
        }
    }

    /**
     * Logging.
     */
    fun setLevel(loglevel: String) {
        val getLogger: ch.qos.logback.classic.Logger = getLogger()
        if (Objects.nonNull(loglevel)) {
            getLogger.level = Level.toLevel(loglevel)
        }
        println("Logging level: " + getLogger.level)
        log.debug("This is a debug message.")
        log.info("This is an info message.")
        log.warn("This is a warn message.")
        log.error("This is an error message.")
    }

    /**
     * Get logger.
     * @return -- logger
     */
    fun getLogger(): ch.qos.logback.classic.Logger {
        return LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
    }
}