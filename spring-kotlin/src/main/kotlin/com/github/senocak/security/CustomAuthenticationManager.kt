package com.github.senocak.security

import com.github.senocak.domain.User
import com.github.senocak.service.UserService
import com.github.senocak.util.RoleName
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationManager(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
): AuthenticationManager {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun authenticate(authentication: Authentication): Authentication {
        val user: User? = userService.findByUsername(authentication.name)
        if (authentication.credentials != null){
            val matches = passwordEncoder.matches(authentication.credentials.toString(), user!!.password)
            if (!matches) {
                log.error("AuthenticationCredentialsNotFoundException occurred for ${user.name}")
                throw AuthenticationCredentialsNotFoundException("Username or password invalid")
            }
        }
        val authorities: MutableCollection<SimpleGrantedAuthority> = ArrayList()
        authorities.add(SimpleGrantedAuthority(RoleName.ROLE_USER.role))
        if (user!!.roles.stream().anyMatch { r -> r.name!! == RoleName.ROLE_ADMIN })
            authorities.add(SimpleGrantedAuthority(RoleName.ROLE_ADMIN.role))

        val loadUserByUsername = userService.loadUserByUsername(authentication.name)
        // val auth: Authentication = UsernamePasswordAuthenticationToken(user, user.password, authorities)
        // we can set either from local or security user
        val auth: Authentication = UsernamePasswordAuthenticationToken(loadUserByUsername, user.password, authorities)
        SecurityContextHolder.getContext().authentication = auth
        log.debug("Authentication is set to SecurityContext for ${user.name}")
        return auth
    }
}