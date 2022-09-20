package com.github.senocak.controller.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.domain.dto.websocket.WebsocketIdentifier
import com.github.senocak.domain.dto.websocket.WsRequestBody
import com.github.senocak.service.WebSocketCacheService
import com.github.senocak.util.AppConstants.getWebsocketIdentifier
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Controller
class WebsocketChannelHandler(
    private val webSocketCacheService: WebSocketCacheService,
    private val objectMapper: ObjectMapper
): AbstractWebSocketHandler() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * A method that is called when a new WebSocket session is created.
     * @param session The new WebSocket session.
     */
    override fun afterConnectionEstablished(session: WebSocketSession) {
        try {
            if (session.uri == null) {
                log.error("Unable to retrieve the websocket session; serious error!")
                return
            }
            val uri = session.uri!!.path
            val websocketIdentifier: WebsocketIdentifier? = getWebsocketIdentifier(uri)
            if (websocketIdentifier?.user == null) {
                log.error("Unable to  extract the websocketIdentifier; serious error!")
                return
            }
            websocketIdentifier.session = session
            webSocketCacheService.put(websocketIdentifier)
            log.debug("Websocket session established: {}", websocketIdentifier)
        } catch (ex: Throwable) {
            log.error("A serious error has occurred with websocket post-connection handling. Exception is: ", ex)
        }
    }

    /**
     * A method that is called when a WebSocket session is closed.
     * @param session The WebSocket session that is closed.
     * @param status The status of the close.
     */
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        try {
            log.debug("afterConnectionClosedWorker() method called.")
            if (session.uri == null) {
                log.error("Unable to retrieve the websocket session; serious error!")
                return
            }
            val uri = session.uri!!.path
            val websocketIdentifier: WebsocketIdentifier? = getWebsocketIdentifier(uri)
            if (websocketIdentifier == null) {
                log.error("Unable to extract websocketIdentifier; serious error!")
                return
            }
            webSocketCacheService.deleteSession(websocketIdentifier.user)
            log.debug("Websocket channel {} has been closed", websocketIdentifier.channelId)
        } catch (ex: Throwable) {
            log.error("Error occurred while closing websocket channel:{}", ExceptionUtils.getMessage(ex))
        }
    }

    /**
     * A method that is called when a WebSocket session receives a message.
     * @param session The WebSocket session that received the message.
     * @param message The message received.
     */
    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val uri = session.uri
            if (uri == null) {
                log.error("URI is not found, returning...")
                return
            }
            val websocketIdentifier: WebsocketIdentifier? = getWebsocketIdentifier(uri.path)
            if (websocketIdentifier == null) {
                log.error("Unable to extract websocketIdentifier; serious error!")
                return
            }
            try {
                val requestBody: WsRequestBody = objectMapper.readValue(message.payload, WsRequestBody::class.java)
                requestBody.from = websocketIdentifier.user
                webSocketCacheService.sendPrivateMessage(requestBody)
                log.debug("Websocket message sent: {}", message.payload)
            } catch (ex: Exception) {
                log.error("Unable to parse request body; serious error!", ex)
            }
        } catch (ex: Throwable) {
            log.error("A serious error has occurred with incoming websocket text message handling. Exception is: ", ex)
        }
    }

    @EventListener
    @Throws(Exception::class)
    fun handleSessionConnected(event: SessionConnectEvent) {
        val headers = SimpMessageHeaderAccessor.wrap(event.message)
        val username = headers.user!!.name
        log.debug("SessionConnectEvent: $username")
    }

    @EventListener
    @Throws(Exception::class)
    fun handleSessionDisconnect(event: SessionDisconnectEvent) {
        val headers = SimpMessageHeaderAccessor.wrap(event.message)
        val username = headers.user!!.name
        log.debug("SessionDisconnectEvent: $username")
    }
}