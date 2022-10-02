package com.github.senocak.controller.integration

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.TestConstants
import com.github.senocak.config.SpringBootTestConfig
import com.github.senocak.controller.PostsController
import com.github.senocak.domain.Role
import com.github.senocak.domain.User
import com.github.senocak.domain.dto.comment.CommentRequestDto
import com.github.senocak.domain.dto.comment.CommentWrapperDto
import com.github.senocak.domain.dto.post.CreatePostDto
import com.github.senocak.domain.dto.post.PostUpdateDto
import com.github.senocak.exception.RestExceptionHandler
import com.github.senocak.factory.UserFactory.createRole
import com.github.senocak.factory.UserFactory.createUser
import com.github.senocak.repository.UserRepository
import com.github.senocak.service.UserService
import com.github.senocak.util.AppConstants.toSlug
import com.github.senocak.util.OmaErrorMessageType
import com.github.senocak.util.RoleName
import org.hamcrest.Matchers
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * This integration test class is written for
 * @see PostsController
 * 22 tests
 */
@SpringBootTestConfig
@DisplayName("Integration Tests for PostsController")
class PostsControllerTest {
    @Autowired private lateinit var postsController: PostsController
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var userRepository: UserRepository
    @MockBean private lateinit var userService: UserService
    private lateinit var mockMvc: MockMvc

    private val new_post_name = "lorem ipsum"
    private val updated_post_name = "lorem ipsum 2"

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(postsController)
            .setControllerAdvice(RestExceptionHandler::class.java)
            .build()
    }

    @Nested
    @Order(1)
    @DisplayName("Create new post")
    internal inner class CreateTest {
        private val createPostDto: CreatePostDto = CreatePostDto()

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        @Throws(Exception::class)
        fun givenInvalidSchema_whenCreate_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(PostsController.URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(createPostDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.containsInAnyOrder("title: must not be blank","body: must not be blank")
                ))
        }

        @Test
        @DisplayName("ServerException is expected since same post name is sent")
        @Throws(Exception::class)
        fun givenSamePost_whenCreate_thenThrowServerException() {
            // Given
            createPostDto.title = "API Gateway Pattern"
            createPostDto.body = "API Gateway Pattern"
            val categoryList: MutableList<String> = ArrayList()
            categoryList.add("Spring Boot")
            createPostDto.category = categoryList
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(PostsController.URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(createPostDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.CONFLICT.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.BASIC_INVALID_INPUT.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.BASIC_INVALID_INPUT.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("Post already exist")))
        }

        @Test
        @DisplayName("ServerException is expected since same post name is sent")
        @Throws(Exception::class)
        fun givenInvalidCategory_whenCreate_thenThrowServerException() {
            // Given
            createPostDto.title = new_post_name
            createPostDto.body = new_post_name
            val categoryList: MutableList<String> = ArrayList()
            categoryList.add("invalid")
            createPostDto.category = categoryList
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(PostsController.URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(createPostDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("Category: invalid")))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenCreate_thenReturn200() {
            // Given
            createPostDto.title = new_post_name
            createPostDto.body = new_post_name
            val categoryList: MutableList<String> = ArrayList()
            val cat_slug = "spring-boot"
            categoryList.add(cat_slug)
            createPostDto.category = categoryList
            val tag_name = "lorem"
            val tagList: MutableList<String> = ArrayList()
            tagList.add(tag_name)
            createPostDto.tags = tagList
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(PostsController.URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(createPostDto))
            val user: User = userRepository.findAll().first()
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.resourceId", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.slug", IsEqual.equalTo(toSlug(new_post_name))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.title", IsEqual.equalTo(new_post_name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.body", IsEqual.equalTo(new_post_name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.user.username", IsEqual.equalTo(user.username)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.categories", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.categories[0].slug", IsEqual.equalTo(cat_slug)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.tags", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.tags[0]", IsEqual.equalTo(tag_name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.resourceUrl",
                    IsEqual.equalTo(PostsController.URL + "/" + toSlug(new_post_name))))
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Create comment to new post")
    internal inner class CreateCommentTest {
        private val URL = PostsController.URL + "/{slug}/comment"
        private val commentRequestDto: CommentRequestDto = CommentRequestDto()

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        @Throws(Exception::class)
        fun givenInvalidSchema_whenCreateComment_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(URL, new_post_name)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(commentRequestDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.containsInAnyOrder("name: must not be blank","body: must not be blank")))
        }

        @Test
        @DisplayName("ServerException is expected since post not exist")
        @Throws(Exception::class)
        fun givenInvalidPost_whenCreateComment_thenThrowServerException() {
            // Given
            commentRequestDto.name = TestConstants.COMMENT_TITLE
            commentRequestDto.body = TestConstants.COMMENT_BODY
            commentRequestDto.email = TestConstants.COMMENT_EMAIL
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(URL, new_post_name)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(commentRequestDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("Post: " + new_post_name)))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenCreateComment_thenReturn201() {
            // Given
            commentRequestDto.name = TestConstants.COMMENT_TITLE
            commentRequestDto.body = TestConstants.COMMENT_BODY
            commentRequestDto.email = TestConstants.COMMENT_EMAIL
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(URL, toSlug(new_post_name))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(commentRequestDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            val response: CommentWrapperDto = objectMapper.readValue(
                perform.andReturn().response.contentAsString,
                CommentWrapperDto::class.java
            )
            Assertions.assertNotNull(response)
            Assertions.assertNotNull(response.commentDto)
            Assertions.assertNotNull(response.commentDto!!.resourceId)
            NEW_COMMENT_ID = response.commentDto!!.resourceId
            Assertions.assertEquals(TestConstants.COMMENT_TITLE, response.commentDto!!.name)
            Assertions.assertEquals(TestConstants.COMMENT_BODY, response.commentDto!!.body)
            Assertions.assertEquals(TestConstants.COMMENT_EMAIL, response.commentDto!!.email)
            Assertions.assertFalse(response.commentDto!!.approved)
            Assertions.assertEquals(HttpStatus.CREATED.value(), perform.andReturn().response.status)
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Update comment to new post")
    internal inner class UpdateCommentTest {
        private val URL = PostsController.URL + "/{slug}/comment/{commentId}?approve={approve}"

        @Test
        @DisplayName("ServerException is expected since post not exist")
        @Throws(Exception::class)
        fun givenInvalidPost_whenUpdateComment_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(URL, TestConstants.CATEGORY_SLUG, "NEW_COMMENT_ID", "true")
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("Post: " + TestConstants.CATEGORY_SLUG)))
        }

        @Test
        @DisplayName("ServerException is expected since comment not exist")
        @Throws(Exception::class)
        fun givenInvalidComment_whenUpdateComment_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(URL, toSlug(new_post_name), "NEW_COMMENT_ID", "true")
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("Comment: NEW_COMMENT_ID")))
        }

        @Test
        @DisplayName("ServerException is expected since comment not belong to proper post")
        @Throws(Exception::class)
        fun givenCommentNotBelongToPost_whenUpdateComment_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(URL, "abstract-document-pattern", NEW_COMMENT_ID, "true")
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.BASIC_INVALID_INPUT.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.BASIC_INVALID_INPUT.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("Comment does not belong the post")))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenCreateComment_thenReturn200Ok() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(URL, toSlug(new_post_name), NEW_COMMENT_ID, "true")
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.comment.resourceId", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comment.name",
                    IsEqual.equalTo(TestConstants.COMMENT_TITLE)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comment.email",
                    IsEqual.equalTo(TestConstants.COMMENT_EMAIL)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comment.body",
                    IsEqual.equalTo(TestConstants.COMMENT_BODY)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comment.approved", IsEqual.equalTo(true)))
        }
    }

    @Nested
    @Order(4)
    @DisplayName("Update post")
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    internal inner class UpdateTest {
        private val url = PostsController.URL + "/{slug}"
        private val postUpdateDto: PostUpdateDto = PostUpdateDto()

        @Test
        @Order(1)
        @DisplayName("ServerException is expected since title size is not valid")
        @Throws(Exception::class)
        fun givenInvalidSchema_whenUpdatePost_thenThrowServerException() {
            // Given
            postUpdateDto.title = "as"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(url, toSlug(new_post_name))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(postUpdateDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("title: size must be between 4 and 40")))
        }

        @Test
        @Order(2)
        @DisplayName("ServerException is expected since post not exist")
        @Throws(Exception::class)
        fun givenInvalidPost_whenUpdateComment_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(url, "invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(postUpdateDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", IsEqual.equalTo("Post: invalid")))
        }

        @Test
        @Order(3)
        @DisplayName("ServerException is expected since comment not belong to proper post")
        @Throws(Exception::class)
        fun givenPostNotBelongToUser_whenUpdateComment_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(url, toSlug(new_post_name))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(postUpdateDto))
            val user: User = createUser()
            val roles: MutableSet<Role> = HashSet<Role>()
            roles.add(createRole(RoleName.ROLE_USER))
            user.roles = roles
            user.email = "email"
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.UNAUTHORIZED.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.UNAUTHORIZED.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.UNAUTHORIZED.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("This user does not have the right permission for this operation")))
        }

        @Test
        @Order(4)
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenUpdateComment_thenReturn200() {
            // Given
            postUpdateDto.title = updated_post_name
            postUpdateDto.body = updated_post_name
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(url, toSlug(new_post_name))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(postUpdateDto))
            val user: User = createUser()
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.resourceId", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.slug", IsEqual.equalTo(toSlug(new_post_name))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.title", IsEqual.equalTo(updated_post_name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.body", IsEqual.equalTo(updated_post_name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.categories", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.comments", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.comments[0].name", IsEqual.equalTo(TestConstants.COMMENT_TITLE)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.comments[0].email", IsEqual.equalTo(TestConstants.COMMENT_EMAIL)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.comments[0].body", IsEqual.equalTo(TestConstants.COMMENT_BODY)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.comments[0].approved", IsEqual.equalTo(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.tags", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.resourceUrl",
                    IsEqual.equalTo(PostsController.URL + "/" + toSlug(new_post_name))))
        }
    }

    @Nested
    @Order(5)
    @DisplayName("Delete post")
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    internal inner class DeleteTest {
        private val url = PostsController.URL + "/{slug}"

        @Test
        @Order(1)
        @DisplayName("ServerException is expected since post not exist")
        @Throws(Exception::class)
        fun givenInvalidPost_whenDeleteComment_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .delete(url, "invalid")
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", IsEqual.equalTo("Post: invalid")))
        }

        @Test
        @Order(2)
        @DisplayName("ServerException is expected since comment not belong to proper post")
        @Throws(Exception::class)
        fun givenCommentNotBelongToUser_whenUpdateComment_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .delete(url, toSlug(new_post_name))
            val user: User = createUser()
            val roles: MutableSet<Role> = HashSet<Role>()
            roles.add(createRole(RoleName.ROLE_USER))
            user.roles = roles
            user.email = "email"
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.UNAUTHORIZED.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.UNAUTHORIZED.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.UNAUTHORIZED.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("This user does not have the right permission for this operation")))
        }

        @Test
        @Order(3)
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenDeleteComment_thenReturn200() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .delete(url, toSlug(new_post_name))
            val user: User = createUser()
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform.andExpect(MockMvcResultMatchers.status().isNoContent)
        }
    }

    @Nested
    @Order(6)
    @DisplayName("Get all posts")
    internal inner class GetAllTest {
        @Test
        @DisplayName("ServerException is expected since post not exist")
        @Throws(Exception::class)
        fun givenInvalidCategory_whenGetAll_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders.get(PostsController.URL + "?category=invalid")
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", IsEqual.equalTo("Category: invalid")))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenGetAll_thenReturn200() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders.get(PostsController.URL)
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.post",Matchers.hasSize<Any>(10)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.next",IsEqual.equalTo(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total",IsEqual.equalTo(52)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resourceUrl",IsEqual.equalTo(PostsController.URL)))
        }
    }

    @Nested
    @Order(7)
    @DisplayName("Get single posts")
    internal inner class GetSingleTest {
        private val url = PostsController.URL + "/{slug}"

        @Test
        @DisplayName("ServerException is expected since post not exist")
        @Throws(Exception::class)
        fun givenInvalidCategory_whenGetSingle_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders.get(url, "invalid")
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", IsEqual.equalTo("Post: invalid")))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenGetSingle_thenReturn200() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders.get(url, "abstract-document-pattern")
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.slug", IsEqual.equalTo("abstract-document-pattern")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post.resourceUrl", IsEqual.equalTo(PostsController.URL + "/abstract-document-pattern")))
        }
    }

    /**
     * @param value -- an object that want to be serialized
     * @return -- string
     * @throws JsonProcessingException -- throws JsonProcessingException
     */
    @Throws(JsonProcessingException::class)
    private fun writeValueAsString(value: Any): String {
        return objectMapper.writeValueAsString(value)
    }

    companion object {
        private var NEW_COMMENT_ID: String? = null
    }
}
