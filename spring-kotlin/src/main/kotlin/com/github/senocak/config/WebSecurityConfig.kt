package com.github.senocak.config

import com.github.senocak.security.JwtAuthenticationEntryPoint
import com.github.senocak.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val unauthorizedHandler: JwtAuthenticationEntryPoint,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    /**
     * Override this method to configure the HttpSecurity.
     * @param http -- It allows configuring web based security for specific http requests
     * @throws Exception -- throws Exception
     */
    @Profile("!integration-test")
    @Throws(Exception::class)
    @Bean
    fun configure(http: HttpSecurity): SecurityFilterChain {
        return http.cors()
            .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                .antMatchers(
                    "/",
                    "/*.html",
                    "/favicon.ico",
                    "/**/*.png",
                    "/**/*.gif",
                    "/**/*.svg",
                    "/**/*.jpg",
                    "/**/*.html",
                    "/**/*.css",
                    "/**/*.js",
                    "/assets/**",
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/api/v1/auth/refresh",
                    "/api/v1/posts/**",
                    "/api/v1/categories/**",
                    "/api/v1/user/**",
                    "/api/v1/swagger/**",
                    "/swagger**/**",
                    "/websocket/**"
                ).permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/posts/**/comment").permitAll()
                .anyRequest().authenticated()
            .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
