package com.github.senocak.controller.websocket

import com.github.senocak.domain.dto.websocket.WebsocketIdentifier
import com.github.senocak.service.WebSocketCacheService
import com.github.senocak.util.AppConstants.getWebsocketIdentifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.util.StringUtils
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

@Controller
class HttpHandshakeInterceptor(
    private val webSocketCacheService: WebSocketCacheService
): HttpSessionHandshakeInterceptor() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Check if session is created.
     * @return true if session is created.
     */
    override fun isCreateSession(): Boolean {
        val isCreateSession = super.isCreateSession()
        log.debug("isCreateSession : {}", isCreateSession)
        return isCreateSession
    }

    /**
     * A method beforeHandshake is called before the WebSocket handshake is completed.
     * @param request the current request
     * @param response the current response
     * @param wsHandler the target WebSocket handler
     * @param attributes attributes from the HTTP handshake to associate with the WebSocket
     * session; the provided attributes are copied, the original map is not used.
     * @return true if the WebSocket handshake should continue, false if the WebSocket
     */
    override fun beforeHandshake(
        request: ServerHttpRequest, response: ServerHttpResponse,
        wsHandler: WebSocketHandler, attributes: Map<String, Any>
    ): Boolean {
        val requestUri = request.uri
        val requestPath = requestUri.path
        val websocketIdentifier: WebsocketIdentifier? = getWebsocketIdentifier(requestPath)
        if (websocketIdentifier == null) {
            log.error("Received an incoming websocket channel request, but was unable to generate it")
            return false
        }
        log.debug("Received an incoming websocket channel request for websocketIdentifier: {}", websocketIdentifier)

        //TODO: Validate the token (if present)
        val queryParams = getQueryParams(requestUri.query)
        if (!validateAccessToken(queryParams, websocketIdentifier.user!!)) {
            log.debug("Token is invalid for this user; rejecting websocket connection attempt!")
            return false
        }
        val headers = request.headers
        if (headers.containsKey("token")) {
            val token = headers["token"]
            if (token != null && token.size > 0)
                websocketIdentifier.token = token.stream().findFirst().get()
        }
        val allWebSocketSession: Map<String, WebsocketIdentifier> = webSocketCacheService.allWebSocketSession
        if (allWebSocketSession.containsKey(websocketIdentifier.user)) {
            log.debug("User already exists in the websocket session cache; rejecting websocket connection attempt!")
            return false
        }
        log.debug("Result for channel {}", websocketIdentifier.channelId)
        return true
    }

    /**
     * A method afterHandshake is called after the WebSocketHandler has been created.
     * @param request the current request
     * @param response the current response
     * @param wsHandler the target WebSocket handler
     * @param ex an exception raised during the handshake, or `null` if none
     */
    override fun afterHandshake(request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler,
                                ex: Exception?) {
        log.debug("afterHandshake")
    }

    /**
     * Validates the access token for the user.
     * @param queryParams The query params from the request.
     * @param user The user to validate the token for.
     * @return True if the token is valid, false otherwise.
     */
    private fun validateAccessToken(queryParams: Map<String, String>?, user: String): Boolean {
        if (queryParams == null) {
            return true
        }
        val accessToken = queryParams["access_token"]
        if (!StringUtils.hasLength(accessToken)) {
            return true
        }
        /*
        if (!TokenUtil.isValid(accessToken, user)) {
            return false;
        }
        */
        log.debug("Valid Access Token : {}", accessToken)
        return true
    }

    /**
     * Parses the query string into a map of key/value pairs.
     * @param queryParamString The query string to parse.
     * @return A map of key/value pairs.
     */
    private fun getQueryParams(queryParamString: String?): Map<String, String>? {
        val queryParams: MutableMap<String, String> = LinkedHashMap()
        if (!StringUtils.hasLength(queryParamString))
            return null
        val split = StringUtils.split(queryParamString, "&")
        if (split != null && split.size > 1) for (param in split) {
            val paramArray = split(param)
            queryParams[paramArray!![0]] = paramArray[1]
        } else {
            val paramArray = split(queryParamString!!)
            queryParams[paramArray!![0]] = paramArray[1]
        }
        return queryParams
    }

    /**
     * Split a string into two parts, separated by a delimiter.
     * @param param The string to split.
     * @return The array of two strings.
     */
    private fun split(param: String): Array<String>? {
        return StringUtils.split(param, "=")
    }
}
