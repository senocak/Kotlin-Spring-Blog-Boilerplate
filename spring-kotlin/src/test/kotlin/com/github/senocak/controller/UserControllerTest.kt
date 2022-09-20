package com.github.senocak.controller

import com.github.senocak.TestConstants
import com.github.senocak.domain.User
import com.github.senocak.domain.dto.user.UpdateUserDto
import com.github.senocak.domain.dto.user.UserWrapperResponse
import com.github.senocak.exception.ServerException
import com.github.senocak.factory.UserFactory.createUser
import com.github.senocak.service.UserService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import javax.servlet.http.HttpServletRequest

@Tag("unit")
@ExtendWith(MockitoExtension::class)
@DisplayName("Unit Tests for UserController")
class UserControllerTest {
    private val userService = Mockito.mock(UserService::class.java)
    private val passwordEncoder = Mockito.mock(PasswordEncoder::class.java)
    private var userController: UserController = UserController(userService, passwordEncoder)
    private val bindingResult = Mockito.mock(BindingResult::class.java)

    @Nested
    internal inner class GetMeTest {
        
        @Test
        @Throws(ServerException::class)
        fun givenServerException_whenGetMe_thenThrowServerException() {
            // Given
            Mockito.doThrow(ServerException::class.java).`when`(userService).loggedInUser()
            // When
            val closureToTest = Executable { userController.me() }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenGetMe_thenReturn200() {
            // Given
            val user: User = createUser()
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            // When
            val getMe: ResponseEntity<UserWrapperResponse> = userController.me()
            // Then
            Assertions.assertNotNull(getMe)
            Assertions.assertNotNull(getMe.statusCode)
            Assertions.assertEquals(HttpStatus.OK, getMe.statusCode)
            Assertions.assertNotNull(getMe.body)
            Assertions.assertNotNull(getMe.body!!.userResponse)
            Assertions.assertEquals(user.username, getMe.body!!.userResponse.username)
            Assertions.assertEquals(user.email, getMe.body!!.userResponse.email)
            Assertions.assertEquals(user.name, getMe.body!!.userResponse.name)
            Assertions.assertEquals(UserController.URL + "/" + user.username,
                getMe.body!!.userResponse.resourceUrl)
            Assertions.assertNull(getMe.body!!.token)
        }
    }

    @Nested
    internal inner class PatchMeTest {
        private val updateUserDto: UpdateUserDto = UpdateUserDto()
        private val httpServletRequest: HttpServletRequest = Mockito.mock(HttpServletRequest::class.java)

        @Test
        @Throws(ServerException::class)
        fun givenNullPasswordConf_whenPatchMe_thenThrowServerException() {
            // Given
            val user: User = createUser()
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            updateUserDto.password = "pass1"
            // When
            val closureToTest = Executable { userController.patchMe(httpServletRequest, updateUserDto, bindingResult) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun givenInvalidPassword_whenPatchMe_thenThrowServerException() {
            // Given
            val user: User = createUser()
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            updateUserDto.password = "pass1"
            updateUserDto.password_confirmation = "pass2"
            // When
            val closureToTest = Executable { userController.patchMe(httpServletRequest, updateUserDto, bindingResult) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenPatchMe_thenThrowServerException() {
            // Given
            val user: User = createUser()
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            updateUserDto.name = TestConstants.USER_NAME
            updateUserDto.password = "pass1"
            updateUserDto.password_confirmation = "pass1"
            // When
            val patchMe = userController.patchMe(httpServletRequest, updateUserDto, bindingResult)
            // Then
            Assertions.assertNotNull(patchMe)
            Assertions.assertNotNull(patchMe.statusCode)
            Assertions.assertEquals(HttpStatus.OK, patchMe.statusCode)
            Assertions.assertNotNull(patchMe.body)
            Assertions.assertEquals(1, patchMe.body!!.size)
            Assertions.assertNotNull(patchMe.body!!["message"])
            Assertions.assertEquals("User updated.", patchMe.body!!["message"])
        }
    }
}