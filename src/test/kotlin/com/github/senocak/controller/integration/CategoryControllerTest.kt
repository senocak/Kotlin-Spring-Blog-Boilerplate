package com.github.senocak.controller.integration

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.config.SpringBootTestConfig
import com.github.senocak.controller.CategoryController
import com.github.senocak.domain.dto.category.CategoryCreateRequestDto
import com.github.senocak.domain.dto.category.CategoryUpdateRequestDto
import com.github.senocak.exception.advice.RestExceptionHandler
import com.github.senocak.util.AppConstants.toSlug
import com.github.senocak.util.OmaErrorMessageType
import org.hamcrest.Matchers
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * This integration test class is written for
 * @see CategoryController
 * 15 tests
 */
@SpringBootTestConfig
@DisplayName("Integration Tests for CategoryController")
class CategoryControllerTest {
    @Autowired private lateinit var categoryController: CategoryController
    @Autowired private lateinit var objectMapper: ObjectMapper

    private lateinit var mockMvc: MockMvc
    private val NEW_CATEGORY_NAME = "lorem ipsum"
    private val UPDATED_CATEGORY_NAME = "lorem ipsum 2"

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
            .setControllerAdvice(RestExceptionHandler::class.java)
            .build()
    }

    @Nested
    @Order(1)
    @DisplayName("Create new category")
    internal inner class CreateTest {
        private val categoryCreateRequestDto: CategoryCreateRequestDto = CategoryCreateRequestDto()

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        @Throws(Exception::class)
        fun givenInvalidSchema_whenCreate_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(CategoryController.URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(categoryCreateRequestDto))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", IsEqual.equalTo("name: must not be null")))
        }

        @Test
        @DisplayName("ServerException is expected since same category name is sent")
        @Throws(Exception::class)
        fun givenSameCategory_whenCreate_thenThrowServerException() {
            // Given
            categoryCreateRequestDto.name = "Spring Boot"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(CategoryController.URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(categoryCreateRequestDto))
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
                    IsEqual.equalTo<Any>("Category " + categoryCreateRequestDto.name + " already exist")))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenCreate_thenReturn200() {
            // Given
            categoryCreateRequestDto.name = NEW_CATEGORY_NAME
            categoryCreateRequestDto.image = NEW_CATEGORY_NAME
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(CategoryController.URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(categoryCreateRequestDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.resourceId", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.name", IsEqual.equalTo(NEW_CATEGORY_NAME)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.slug", IsEqual.equalTo(toSlug(NEW_CATEGORY_NAME))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.image",
                    IsEqual.equalTo(CategoryController.URL + "/" + toSlug(NEW_CATEGORY_NAME) + "/image")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.resourceUrl",
                    IsEqual.equalTo(CategoryController.URL + "/" + toSlug(NEW_CATEGORY_NAME))))
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Update added category")
    internal inner class UpdateTest {
        private val categoryUpdateRequestDto: CategoryUpdateRequestDto = CategoryUpdateRequestDto()

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        @Throws(Exception::class)
        fun givenInvalidSchema_whenCreate_thenThrowServerException() {
            // Given
            categoryUpdateRequestDto.name = "lo"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(CategoryController.URL + "/" + NEW_CATEGORY_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(categoryUpdateRequestDto))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("name: size must be between 3 and 30")))
        }

        @Test
        @DisplayName("ServerException is expected since category not exist")
        @Throws(Exception::class)
        fun givenNotFoundCategory_whenCreate_thenThrowServerException() {
            // Given
            val category = "Invalid Category Name Is Here"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(CategoryController.URL + "/" + category)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(categoryUpdateRequestDto))
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
                    IsEqual.equalTo("Category: " + toSlug(category))))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenCreate_thenReturn200() {
            // Given
            categoryUpdateRequestDto.name = UPDATED_CATEGORY_NAME
            categoryUpdateRequestDto.image = UPDATED_CATEGORY_NAME
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(CategoryController.URL + "/" + toSlug(NEW_CATEGORY_NAME))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(categoryUpdateRequestDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.resourceId", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.name", IsEqual.equalTo(UPDATED_CATEGORY_NAME)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.slug", IsEqual.equalTo(toSlug(NEW_CATEGORY_NAME))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.image",
                    IsEqual.equalTo(CategoryController.URL + "/" + toSlug(NEW_CATEGORY_NAME) + "/image")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.resourceUrl",
                    IsEqual.equalTo(CategoryController.URL + "/" + toSlug(NEW_CATEGORY_NAME))))
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Delete added category")
    internal inner class DeleteTest {
        @Test
        @DisplayName("ServerException is expected since category not exist")
        @Throws(Exception::class)
        fun givenNotFoundCategory_whenDelete_thenThrowServerException() {
            // Given
            val category = "Invalid Category Name Is Here"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders.delete(CategoryController.URL + "/" + category)
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
                    IsEqual.equalTo("Category: " + toSlug(category))))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenDelete_thenReturn204() {
            // Given;
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .delete(CategoryController.URL + "/" + toSlug(NEW_CATEGORY_NAME))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform.andExpect(MockMvcResultMatchers.status().isNoContent)
        }
    }

    @Nested
    @Order(4)
    @DisplayName("Get all categories")
    internal inner class GetAllTest {
        @Test
        @DisplayName("ServerException is expected since invalid next query param is sent")
        @Throws(Exception::class)
        fun givenInvalidNextQueryParam_whenGetAll_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders.get(CategoryController.URL + "?next=ipsum")
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
                    IsEqual.equalTo("Failed to convert value of type 'java.lang.String' to required type 'int'; nested exception is java.lang.NumberFormatException: For input string: \"ipsum\"")))
        }

        @Test
        @DisplayName("ServerException is expected since invalid max query param is sent")
        @Throws(Exception::class)
        fun givenInvalidMaxQueryParam_whenGetAll_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders.get(CategoryController.URL + "?max=-1")
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
                    IsEqual.equalTo("getAll.maxNumber: must be greater than or equal to 0")))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenGetAll_thenReturn200() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders.get(CategoryController.URL)
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.category", Matchers.hasSize<Any>(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category[0].name", IsEqual.equalTo("Spring Boot")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category[0].slug", IsEqual.equalTo("spring-boot")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category[0].image",
                    IsEqual.equalTo(CategoryController.URL + "/spring-boot/image")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category[0].resourceUrl",
                    IsEqual.equalTo(CategoryController.URL + "/spring-boot")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.next", IsEqual.equalTo(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total", IsEqual.equalTo(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resourceUrl", IsEqual.equalTo(CategoryController.URL)))
        }
    }

    @Nested
    @Order(5)
    @DisplayName("Get single category")
    internal inner class GetSingleTest {
        @Test
        @DisplayName("ServerException is expected since invalid category name is sent")
        @Throws(Exception::class)
        fun givenInvalidCategoryName_whenGetSingle_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders.get(CategoryController.URL + "/slug")
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
                    IsEqual.equalTo("Category: slug")))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenGetSingle_thenReturn200() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders.get(CategoryController.URL + "/spring-boot")
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.name", IsEqual.equalTo("Spring Boot")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.slug", IsEqual.equalTo("spring-boot")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.image",
                    IsEqual.equalTo(CategoryController.URL + "/spring-boot/image")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.category.resourceUrl",
                    IsEqual.equalTo(CategoryController.URL + "/spring-boot")))
        }
    }

    @Nested
    @Order(6)
    @DisplayName("Get image of category")
    internal inner class GetImageOfCategoryTest {
        @Test
        @DisplayName("ServerException is expected since category not exist")
        @Throws(Exception::class)
        fun givenNotFoundCategory_whenGetImageOfCategory_thenThrowServerException() {
            // Given
            val category = "Invalid Category Name Is Here"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .get(CategoryController.URL + "/" + category + "/image")
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
                    IsEqual.equalTo("Category: $category")))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenGetImageOfCategory_thenReturn200() {
            // Given;
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .get(CategoryController.URL + "/spring-boot/image")
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", IsNull.notNullValue()))
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
}
