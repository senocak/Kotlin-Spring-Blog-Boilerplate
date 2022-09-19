package com.github.senocak.service

import com.github.senocak.domain.dto.websocket.WebsocketIdentifier
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.PingMessage
import java.text.SimpleDateFormat
import java.util.Date

@Component
@EnableScheduling
class ScheduledTasks(private val webSocketCacheService: WebSocketCacheService){
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    /**
     * this is scheduled to run every minute
     */
    @Scheduled(cron = "0 * * ? * *")
    fun checkPostsCreated() {
        log.info("The time executed: {}", dateFormat.format(Date()))
    }

    /**
     * this is scheduled to run every in 10_000 milliseconds period // every 10 seconds
     */
    @Scheduled(fixedRate = 10_000)
    fun pingWs() {
        val allWebSocketSession: Map<String, WebsocketIdentifier> = webSocketCacheService.allWebSocketSession
        if (allWebSocketSession.isNotEmpty())
            for (entry in allWebSocketSession) {
                try {
                    entry.value.session!!.sendMessage(PingMessage())
                    log.debug("Pinged user with key: {}, and session: {}", entry.key, entry.value)
                } catch (e: Exception) {
                    log.error("Exception occurred for sending ping message: {}", ExceptionUtils.getMessage(e))
                    webSocketCacheService.deleteSession(entry.key)
                }
            }
    }
}