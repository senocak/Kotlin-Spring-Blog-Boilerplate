package com.github.senocak.service

import com.github.senocak.domain.dto.websocket.WebsocketIdentifier
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.PingMessage
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

@Component
@EnableScheduling
class ScheduledTasks(private val webSocketCacheService: WebSocketCacheService){
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    private val dateFormat = SimpleDateFormat("HH:mm:ss")
    private val byte = 1L
    private val kb: Long = byte * 1000
    private val mb = kb * 1000
    private val gb = mb * 1000
    private val tb = gb * 1000

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

    /**
     * this is scheduled to run every minute
     */
    @Scheduled(cron = "0 * * ? * *")
    fun checkPostsCreated() {
        val runtime = Runtime.getRuntime()
        log.info("The time executed: ${dateFormat.format(Date())}, " +
                "availableProcessors: ${runtime.availableProcessors()}, " +
                "totalMemory: ${toHumanReadableSIPrefixes(runtime.totalMemory())}, " +
                "maxMemory: ${toHumanReadableSIPrefixes(runtime.maxMemory())}, " +
                "freeMemory: ${toHumanReadableSIPrefixes(runtime.freeMemory())}")
    }

    private fun toHumanReadableSIPrefixes(size: Long): String {
        require(size >= 0) { "Invalid file size: $size" }
        if (size >= tb)
            return formatSize(size, tb, "TB")
        if (size >= gb)
            return formatSize(size, gb, "GB")
        if (size >= mb)
            return formatSize(size, mb, "MB")
        if (size >= kb)
            return formatSize(size, kb, "KB")
        return formatSize(size, byte, "Bytes")
    }

    private fun formatSize(size: Long, divider: Long, unitName: String): String {
        return DecimalFormat("#.##").format(size.toDouble() / divider) + " " + unitName
    }
}