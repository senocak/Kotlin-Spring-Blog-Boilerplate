package com.github.senocak.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.domain.dto.websocket.WebsocketIdentifier
import com.github.senocak.domain.dto.websocket.WsRequestBody
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import java.io.IOException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class WebSocketCacheService(private val objectMapper: ObjectMapper) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Get all websocket session cache.
     * @return map of websocket session cache.
     */
    val allWebSocketSession: Map<String, WebsocketIdentifier>
        get() = userSessionCache

    /**
     * Add websocket session cache.
     * @param data websocket session cache.
     */
    fun put(data: WebsocketIdentifier) {
        userSessionCache[data.user!!] = data
        broadCastMessage(data.user!!, "login")
        broadCastAllUserList(data.user!!)
    }

    /**
     * Get or default websocket session cache.
     * @param key key of websocket session cache.
     * @return websocket session cache.
     */
    fun getOrDefault(key: String?): WebsocketIdentifier? {
        return userSessionCache.getOrDefault(key, null)
    }

    /**
     * Remove websocket session cache.
     * @param key key of websocket session cache.
     */
    fun deleteSession(key: String?) {
        val websocketIdentifier: WebsocketIdentifier? = getOrDefault(key)
        if (websocketIdentifier?.session == null) {
            log.error("Unable to remove the websocket session; serious error!")
            return
        }
        userSessionCache.remove(key)
        broadCastAllUserList(websocketIdentifier.user!!)
        broadCastMessage(websocketIdentifier.user!!, "logout")
    }

    /**
     * Broadcast message to all websocket session cache.
     * @param message message to broadcast.
     */
    private fun broadCastMessage(message: String, type: String) {
        val wsRequestBody = WsRequestBody()
        wsRequestBody.content = message
        wsRequestBody.date = Instant.now().toEpochMilli()
        wsRequestBody.type = type
        for (entry in allWebSocketSession) {
            try {
                entry.value.session!!.sendMessage(TextMessage(objectMapper.writeValueAsString(wsRequestBody)))
            } catch (e: Exception) {
                log.error("Exception while broadcasting: {}", ExceptionUtils.getMessage(e))
            }
        }
    }

    /**
     * Broadcast message to specific websocket session cache.
     * @param requestBody message to send.
     */
    fun sendPrivateMessage(requestBody: WsRequestBody) {
        val userTo: WebsocketIdentifier? = getOrDefault(requestBody.to)
        if (userTo?.session == null) {
            log.error("User or Session not found in cache for user: {}, returning...", requestBody.to)
            return
        }
        requestBody.type = "private"
        requestBody.date = Instant.now().toEpochMilli()
        try {
            userTo.session!!.sendMessage(TextMessage(objectMapper.writeValueAsString(requestBody)))
        } catch (e: IOException) {
            log.error("Exception while sending message: {}", ExceptionUtils.getMessage(e))
        }
    }

    /**
     * Broadcast message to specific websocket session cache.
     * @param from from user.
     * @param payload message to send.
     */
    fun sendMessage(from: String?, to: String?, type: String?, payload: String?) {
        val userTo: WebsocketIdentifier? = getOrDefault(to)
        if (userTo?.session == null) {
            log.error("User or Session not found in cache for user: {}, returning...", to)
            return
        }
        val requestBody = WsRequestBody()
        requestBody.from = from
        requestBody.to = to
        requestBody.date = Instant.now().toEpochMilli()
        requestBody.content = payload
        requestBody.type = type
        try {
            userTo.session!!.sendMessage(TextMessage(objectMapper.writeValueAsString(requestBody)))
        } catch (e: IOException) {
            log.error("Exception while sending message: {}", ExceptionUtils.getMessage(e))
        }
    }

    /**
     * Broadcast message to all websocket session cache.
     * @param user user to broadcast.
     */
    private fun broadCastAllUserList(user: String) {
        sendMessage("server", user, "online", StringUtils.join(userSessionCache.keys, ','))
    }

    companion object {
        private val userSessionCache: MutableMap<String, WebsocketIdentifier> =
            ConcurrentHashMap<String, WebsocketIdentifier>()
    }
}