package com.github.senocak.controller

import com.github.senocak.domain.ExceptionDto
import com.github.senocak.domain.Role
import com.github.senocak.domain.User
import com.github.senocak.domain.dto.auth.LoginRequest
import com.github.senocak.domain.dto.auth.RegisterRequest
import com.github.senocak.domain.dto.user.UserResponse
import com.github.senocak.domain.dto.user.UserWrapperResponse
import com.github.senocak.exception.ServerException
import com.github.senocak.security.JwtTokenProvider
import com.github.senocak.service.DtoConverter
import com.github.senocak.service.RoleService
import com.github.senocak.service.UserService
import com.github.senocak.util.OmaErrorMessageType
import com.github.senocak.util.RoleName
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Objects
import java.util.stream.Collectors

@RestController
@RequestMapping(AuthController.URL)
@Tag(name = "Authentication", description = "AUTH API")
class AuthController(
    private val userService: UserService,
    private val roleService: RoleService,
    private val tokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager
): BaseController() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/login")
    @Operation(summary = "Login Endpoint", tags = ["Authentication"])
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = UserWrapperResponse::class)))),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ]
    )
    @Throws(ServerException::class)
    fun login(
        @Parameter(description = "Request body to login", required = true) @Validated @RequestBody loginRequest: LoginRequest,
        resultOfValidation: BindingResult
    ): ResponseEntity<UserWrapperResponse> {
        validate(resultOfValidation)
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
        )
        val user: User? = userService.findByUsername(loginRequest.username!!)
        val login: UserResponse = DtoConverter.convertEntityToDto(user!!)
        return ResponseEntity.ok(generateUserWrapperResponse(login))
    }

    @PostMapping("/register")
    @Operation(
        summary = "Register Endpoint",
        tags = ["Authentication"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = UserWrapperResponse::class)))),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ]
    )
    @Throws(ServerException::class)
    fun register(
        @Parameter(description = "Request body to register", required = true) @Validated @RequestBody signUpRequest: RegisterRequest,
        resultOfValidation: BindingResult
    ): ResponseEntity<UserWrapperResponse> {
        validate(resultOfValidation)
        if (userService.existsByUsername(signUpRequest.username!!)) {
            log.error("Username:{} is already taken!", signUpRequest.username)
            throw ServerException(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR,
                arrayOf("Username is already taken!"), HttpStatus.BAD_REQUEST)
        }
        if (userService.existsByEmail(signUpRequest.email!!)) {
            log.error("Email Address:{} is already taken!", signUpRequest.email)
            throw ServerException(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR,
                arrayOf("Email Address already in use!"), HttpStatus.BAD_REQUEST)
        }
        val userRole: Role? = roleService.findByName(RoleName.ROLE_USER)
        if (Objects.isNull(userRole)) {
            log.error("User Role is not found")
            throw ServerException(OmaErrorMessageType.MANDATORY_INPUT_MISSING,
                arrayOf("User Role is not found"), HttpStatus.BAD_REQUEST)
        }
        val user = User(signUpRequest.name!!, signUpRequest.username!!, signUpRequest.email!!,
            passwordEncoder.encode(signUpRequest.password), setOf(userRole!!))
        val result: User = userService.save(user)
        log.info("User created. User: {}", result)
        val `object`: UserWrapperResponse? = try {
            login(LoginRequest(signUpRequest.username, signUpRequest.password), resultOfValidation).body
        } catch (e: Exception) {
            throw ServerException(OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                arrayOf("Error occurred for generating jwt attempt", HttpStatus.INTERNAL_SERVER_ERROR.toString()),
                HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(`object`)
    }

    /**
     * Generate UserWrapperResponse with given UserResponse
     * @param userResponse -- UserResponse that contains user data
     * @return UserWrapperResponse
     */
    private fun generateUserWrapperResponse(userResponse: UserResponse): UserWrapperResponse {
        val roles: List<String> = userResponse.roles.stream().map { r -> RoleName.fromString(r.name!!.name)!!.name }.collect(Collectors.toList())
        val generatedToken = tokenProvider.generateJwtToken(userResponse.username, roles)
        val userWrapperResponse = UserWrapperResponse(userResponse, generatedToken)
        log.info("UserWrapperResponse is generated. UserWrapperResponse: {}", userWrapperResponse)
        return userWrapperResponse
    }

    companion object {
        const val URL = "/api/v1/auth"
    }
}