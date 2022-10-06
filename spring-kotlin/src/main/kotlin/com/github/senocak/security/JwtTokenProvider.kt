package com.github.senocak.security

import com.github.senocak.domain.dto.auth.UserInfoCache
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import net.jodah.expiringmap.ExpiringMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import java.lang.Exception
import java.util.Date
import java.util.concurrent.TimeUnit

@Component
class JwtTokenProvider {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    private var jwtSecret: String
    private var jwtExpirationInMs: String
    private var refreshExpirationInMs: String
    private var tokenEventMap: ExpiringMap<String, UserInfoCache>

    constructor(@Value("\${app.jwtSecret}") jSecret: String,
                @Value("\${app.jwtExpirationInMs}") jExpirationInMs: String,
                @Value("\${app.refreshExpirationInMs}") rExpirationInMs: String) {
        jwtSecret = jSecret
        jwtExpirationInMs = jExpirationInMs
        refreshExpirationInMs = rExpirationInMs
        tokenEventMap = ExpiringMap.builder().variableExpiration().build()
    }

    /**
     * Generating the jwt token
     * @param username -- userName
     */
    fun generateJwtToken(username: String, roles: List<String?>): String {
        val token = generateToken(username, roles, jwtExpirationInMs.toLong())
        val userInfoCache = UserInfoCache(username, token, "jwt", jwtExpirationInMs.toLong())
        tokenEventMap.put(token, userInfoCache, jwtExpirationInMs.toLong(), TimeUnit.MILLISECONDS)
        return token
    }

    /**
     * Generating the refresh token
     * @param username -- userName
     */
    fun generateRefreshToken(username: String, roles: List<String?>): String {
        val token = generateToken(username, roles, refreshExpirationInMs.toLong())
        val userInfoCache = UserInfoCache(username, token, "refresh", refreshExpirationInMs.toLong())
        tokenEventMap.put(token, userInfoCache, refreshExpirationInMs.toLong(), TimeUnit.MILLISECONDS)
        return token
    }

    /**
     * Generating the token
     * @param subject -- userName
     */
    private fun generateToken(subject: String, roles: List<String?>, expirationInMs: Long): String {
        val now = Date()
        val claims: MutableMap<String, Any> = HashMap()
        claims["roles"] = roles
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + expirationInMs))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact()
    }

    /**
     * Get the jws claims
     * @param token -- jwt token
     * @return -- expiration date
     */
    private fun getJwsClaims(token: String): Jws<Claims?> {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token)
    }

    /**
     * @param token -- jwt token
     * @return -- userName from jwt
     */
    fun getUserNameFromJWT(token: String): String {
        return getJwsClaims(token).body!!.subject
    }

    /**
     * @param token -- jwt token
     */
    fun validateToken(token: String) {
        try {
            getJwsClaims(token)
            tokenEventMap[token] ?: throw Exception("Token could not found in local cache")
        } catch (ex: SignatureException) {
            log.error("Invalid JWT signature")
            throw AccessDeniedException("Invalid JWT signature")
        } catch (ex: MalformedJwtException) {
            log.error("Invalid JWT token")
            throw AccessDeniedException("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            log.error("Expired JWT token")
            throw AccessDeniedException("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            log.error("Unsupported JWT token")
            throw AccessDeniedException("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            log.error("JWT claims string is empty.")
            throw AccessDeniedException("JWT claims string is empty.")
        }
    }

    fun markLogoutEventForToken(username: String) {
        tokenEventMap.filter { m -> m.value.username == username }
            .forEach { m ->
                run {
                    if (tokenEventMap.containsKey(m.key)) {
                        tokenEventMap.remove(m.key)
                    }
                }
            }
        log.debug("Logged out. Tokens for user ${username} removed in the cache")
    }
}