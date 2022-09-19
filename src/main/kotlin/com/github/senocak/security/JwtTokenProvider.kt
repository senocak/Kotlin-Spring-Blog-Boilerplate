package com.github.senocak.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtTokenProvider {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${app.jwtSecret}") private val jwtSecret: String? = null
    @Value("\${app.jwtExpirationInMs}") private val jwtExpirationInMs = 0

    /**
     * Generating the jwt token
     * @param subject -- userName
     */
    fun generateJwtToken(subject: String, roles: List<String?>): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)
        val claims: MutableMap<String, Any> = HashMap()
        claims["roles"] = roles
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact()
    }

    /**
     * @param token -- jwt token
     * @return -- userName from jwt
     */
    fun getUserNameFromJWT(token: String?): String {
        val claims: Claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .body
        return claims.subject
    }

    /**
     * @param token -- jwt token
     */
    fun validateToken(token: String) {
        try {
            getJwsClaims(token)
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

    /**
     * Get the jws claims
     * @param token -- jwt token
     * @return -- expiration date
     */
    private fun getJwsClaims(token: String): Jws<Claims?> {
        return Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
    }
}