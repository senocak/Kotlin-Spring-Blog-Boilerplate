package com.github.senocak.controller

import com.github.senocak.domain.ExceptionDto
import com.github.senocak.domain.User
import com.github.senocak.domain.dto.user.UpdateUserDto
import com.github.senocak.domain.dto.user.UserResponse
import com.github.senocak.domain.dto.user.UserWrapperResponse
import com.github.senocak.exception.ServerException
import com.github.senocak.security.Authorize
import com.github.senocak.service.DtoConverter.convertEntityToDto
import com.github.senocak.service.UserService
import com.github.senocak.util.AppConstants.ADMIN
import com.github.senocak.util.AppConstants.USER
import com.github.senocak.util.AppConstants.securitySchemeName
import com.github.senocak.util.OmaErrorMessageType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@Authorize(roles = [ADMIN, USER])
@RequestMapping(UserController.URL)
@Tag(name = "User", description = "User Controller")
class UserController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
): BaseController() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Throws(ServerException::class)
    @Operation(
        summary = "Get me",
        tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = UserWrapperResponse::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @GetMapping("/me")
    fun me(): ResponseEntity<UserWrapperResponse> {
        val userResponse: UserResponse = convertEntityToDto(userService.loggedInUser()!!)
        val userWrapperResponse = UserWrapperResponse(userResponse, null)
        return ResponseEntity.ok(userWrapperResponse)
    }

    @PatchMapping("/me")
    @Operation(
        summary = "Update user by username",
        tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = HashMap::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @Throws(ServerException::class)
    fun patchMe(request: HttpServletRequest,
        @Parameter(description = "Request body to update", required = true) @Validated @RequestBody userDto: UpdateUserDto,
        resultOfValidation: BindingResult
    ): ResponseEntity<Map<String, String>> {
        validate(resultOfValidation)
        val user: User? = userService.loggedInUser()
        val name: String? = userDto.name
        if (!name.isNullOrEmpty())
            user!!.name = name
        val password: String? = userDto.password
        val passwordConfirmation: String? = userDto.password_confirmation
        if (!password.isNullOrEmpty()) {
            if (passwordConfirmation.isNullOrEmpty()) {
                val message = "Password confirmation not provided"
                log.error(message)
                throw ServerException(OmaErrorMessageType.BASIC_INVALID_INPUT, arrayOf(message), HttpStatus.BAD_REQUEST)
            }
            if (passwordConfirmation != password) {
                val message = "Password and confirmation not matched"
                log.error(message)
                throw ServerException(OmaErrorMessageType.BASIC_INVALID_INPUT, arrayOf(message), HttpStatus.BAD_REQUEST)
            }
            user!!.password = passwordEncoder.encode(password)
        }
        userService.save(user!!)
        val response: MutableMap<String, String> = HashMap()
        response["message"] = "User updated."
        return ResponseEntity.ok(response)
    }

    companion object {
        const val URL = "/api/v1/user"
    }
}
