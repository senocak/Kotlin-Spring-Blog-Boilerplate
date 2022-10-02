package com.github.senocak.security

import com.github.senocak.exception.RestExceptionHandler
import com.github.senocak.service.UserService
import com.github.senocak.util.AppConstants.TOKEN_HEADER_NAME
import com.github.senocak.util.AppConstants.TOKEN_PREFIX
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Filter class that aims to guarantee a single execution per request dispatch, on any servlet container.
 * @return -- an JwtAuthenticationFilter instance
 */
@Component
class JwtAuthenticationFilter(
    private val tokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
    private val authenticationManager: AuthenticationManager
): OncePerRequestFilter() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Guaranteed to be just invoked once per request within a single request thread.
     *
     * @param request -- Request information for HTTP servlets.
     * @param response -- It is where the servlet can write information about the data it will send back.
     * @param filterChain -- An object provided by the servlet container to the developer giving a view into the invocation chain of a filtered request for a resource.
     * @throws ServletException -- Throws ServletException
     * @throws IOException -- Throws IOException
     */
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val bearerToken = request.getHeader(TOKEN_HEADER_NAME)
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
                val jwt = bearerToken.substring(7)
                try {
                    tokenProvider.validateToken(jwt)
                    val userName: String = tokenProvider.getUserNameFromJWT(jwt)
                    val userDetails: UserDetails = userService.loadUserByUsername(userName)
                    val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.authorities
                    )
                    usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    authenticationManager.authenticate(usernamePasswordAuthenticationToken)
                    logger.trace("SecurityContext created")
                } catch (exception: Exception) {
                    val responseEntity: ResponseEntity<Any> = RestExceptionHandler()
                        .handleUnAuthorized(RuntimeException(exception.message))
                    response.writer.write(objectMapper.writeValueAsString(responseEntity.body))
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.contentType = "application/json"
                    return
                }
            }
        } catch (ex: Exception) {
            log.error("Could not set user authentication in security context. Error: {}",
                ExceptionUtils.getMessage(ex))
        }
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT")
        response.setHeader("Access-Control-Allow-Headers",
            "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With")
        response.setHeader("Access-Control-Expose-Headers",
            "Content-Type, Access-Control-Expose-Headers, Authorization, X-Requested-With")
        filterChain.doFilter(request, response)
        logger.info(request.remoteAddr)
    }
}