package com.github.senocak.controller

import com.github.senocak.TestConstants.CATEGORY_SLUG
import com.github.senocak.domain.Category
import com.github.senocak.domain.dto.category.CategoriesDto
import com.github.senocak.domain.dto.category.CategoryCreateRequestDto
import com.github.senocak.domain.dto.category.CategoryUpdateRequestDto
import com.github.senocak.domain.dto.category.CategoryWrapperDto
import com.github.senocak.exception.ServerException
import com.github.senocak.factory.CategoryFactory
import com.github.senocak.service.CategoryService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult

@Tag("unit")
@ExtendWith(MockitoExtension::class)
@DisplayName("Unit Tests for CategoryController")
class CategoryControllerTest {
    private val categoryService = Mockito.mock(CategoryService::class.java)
    private val bindingResult = Mockito.mock(BindingResult::class.java)
    private var categoryController = CategoryController(categoryService)

    @Nested
    internal inner class CreateTest {
        private val categoryCreateRequestDto: CategoryCreateRequestDto = CategoryCreateRequestDto()

        @Test
        @Throws(ServerException::class)
        fun given_whenCreate_thenReturn201() {
            // Given
            val cName = "name"
            categoryCreateRequestDto.name = cName
            val cImage = "image"
            categoryCreateRequestDto.image = cImage
            val category = Category()
            category.name = cName
            category.image = cImage
            category.slug = CATEGORY_SLUG
            Mockito.doReturn(category).`when`(categoryService).createCategory(categoryCreateRequestDto)
            // When
            val create: ResponseEntity<CategoryWrapperDto> = categoryController.create(categoryCreateRequestDto, bindingResult)
            // Then
            Assertions.assertNotNull(create)
            Assertions.assertNotNull(create.statusCode)
            Assertions.assertEquals(HttpStatus.CREATED, create.statusCode)
            Assertions.assertNotNull(create.body)
            Assertions.assertNotNull(create.body!!.categoryDto)
            Assertions.assertEquals(cName, create.body!!.categoryDto!!.name)
            Assertions.assertEquals(CATEGORY_SLUG, create.body!!.categoryDto!!.slug)
            Assertions.assertEquals(CategoryController.URL + "/" + CATEGORY_SLUG + "/image",
                create.body!!.categoryDto!!.image)
            Assertions.assertNull(create.body!!.categoryDto!!.postDto)
        }
    }

    @Nested
    internal inner class GetAllTest {
        @Test
        fun given_whenGetAll_thenReturn200() {
            // Given
            val categoryList: MutableList<Category> = ArrayList()
            val createCategory: Category = CategoryFactory.createCategory()
            categoryList.add(createCategory)
            val categories: Page<Category> = PageImpl(categoryList)
            Mockito.doReturn(categories).`when`(categoryService).getAll(0, 0)
            // When
            val getAll: ResponseEntity<CategoriesDto> = categoryController.getAll(0, 0, "false")
            // Then
            Assertions.assertNotNull(getAll)
            Assertions.assertNotNull(getAll.statusCode)
            Assertions.assertEquals(HttpStatus.OK, getAll.statusCode)
            Assertions.assertNotNull(getAll.body)
            Assertions.assertEquals(CategoryController.URL, getAll.body!!.resourceUrl)
            Assertions.assertEquals(0, getAll.body!!.next)
            Assertions.assertEquals(categoryList.size.toLong(), getAll.body!!.total)
            Assertions.assertEquals(categoryList.size, getAll.body!!.categoryDtoList!!.size)
            Assertions.assertEquals(
                createCategory.id,
                getAll.body!!.categoryDtoList!![0].resourceId
            )
            Assertions.assertEquals(createCategory.name, getAll.body!!.categoryDtoList!![0].name)
            Assertions.assertEquals(createCategory.slug, getAll.body!!.categoryDtoList!![0].slug)
            Assertions.assertEquals(
                CategoryController.URL + "/" + createCategory.slug + "/image",
                getAll.body!!.categoryDtoList!![0].image
            )
            Assertions.assertEquals(
                CategoryController.URL + "/" + createCategory.slug,
                getAll.body!!.categoryDtoList!![0].resourceUrl
            )
            Assertions.assertNull(getAll.body!!.categoryDtoList!![0].postDto)
        }
    }

    @Nested
    internal inner class UpdateTest {
        private val categoryUpdateRequestDto: CategoryUpdateRequestDto = CategoryUpdateRequestDto()

        @Test
        @Throws(ServerException::class)
        fun given_whenUpdate_thenReturn200() {
            // Given
            categoryUpdateRequestDto.name = "NAME"
            val category: Category = CategoryFactory.createCategory()
            Mockito.doReturn(category).`when`(categoryService).findCategory(CATEGORY_SLUG)
            Mockito.doReturn(category).`when`(categoryService).updateCategory(category, categoryUpdateRequestDto)
            // When
            val create: ResponseEntity<CategoryWrapperDto>? =
                categoryController.update(CATEGORY_SLUG, categoryUpdateRequestDto, bindingResult)
            // Then
            Assertions.assertNotNull(create)
            Assertions.assertNotNull(create!!.statusCode)
            Assertions.assertEquals(HttpStatus.OK, create.statusCode)
            Assertions.assertNotNull(create.body)
            Assertions.assertNotNull(create.body!!.categoryDto)
            Assertions.assertEquals(category.name, create.body!!.categoryDto!!.name)
            Assertions.assertEquals(CATEGORY_SLUG, create.body!!.categoryDto!!.slug)
            Assertions.assertEquals(
                CategoryController.URL + "/" + CATEGORY_SLUG + "/image",
                create.body!!.categoryDto!!.image
            )
            Assertions.assertNull(create.body!!.categoryDto!!.postDto)
        }
    }

    @Nested
    internal inner class DeleteTest {
        @Test
        @Throws(ServerException::class)
        fun given_whenDelete_thenReturn200() {
            // Given
            val category: Category = CategoryFactory.createCategory()
            Mockito.doReturn(category).`when`(categoryService).findCategory(CATEGORY_SLUG)
            // When
            val create = categoryController.delete(CATEGORY_SLUG)
            // Then
            Assertions.assertNotNull(create)
            Assertions.assertNotNull(create!!.statusCode)
            Assertions.assertEquals(HttpStatus.NO_CONTENT, create.statusCode)
        }
    }

    @Nested
    internal inner class GetSingleTest {
        @Test
        @Throws(ServerException::class)
        fun given_whenGetSingle_thenReturn200() {
            // Given
            val category: Category = CategoryFactory.createCategory()
            Mockito.doReturn(category).`when`(categoryService).findCategory(CATEGORY_SLUG)
            // When
            val getSingle: ResponseEntity<CategoryWrapperDto>? = categoryController.getSingle(CATEGORY_SLUG, "false")
            // Then
            Assertions.assertNotNull(getSingle)
            Assertions.assertNotNull(getSingle!!.statusCode)
            Assertions.assertEquals(HttpStatus.OK, getSingle.statusCode)
            Assertions.assertNotNull(getSingle.body)
            Assertions.assertNotNull(getSingle.body!!.categoryDto)
            Assertions.assertEquals(category.id, getSingle.body!!.categoryDto!!.resourceId)
            Assertions.assertEquals(category.name, getSingle.body!!.categoryDto!!.name)
            Assertions.assertEquals(category.slug, getSingle.body!!.categoryDto!!.slug)
            Assertions.assertEquals(CategoryController.URL + "/" + category.slug + "/image",
                getSingle.body!!.categoryDto!!.image)
            Assertions.assertEquals(CategoryController.URL + "/" + category.slug,
                getSingle.body!!.categoryDto!!.resourceUrl)
            Assertions.assertNull(getSingle.body!!.categoryDto!!.postDto)
        }
    }

    @Nested
    internal inner class GetImageOfCategoryTest {
        @Test
        @Throws(ServerException::class)
        fun givenImageNull_whenGetImageOfCategory_thenThrowServerException() {
            // Given
            val createCategory: Category = CategoryFactory.createCategory()
            createCategory.image = null
            Mockito.doReturn(createCategory).`when`(categoryService).findCategory(CATEGORY_SLUG)
            // When
            val closureToTest = Executable { categoryController.getImageOfCategory(CATEGORY_SLUG) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenGetSingle_thenReturn200() {
            // Given
            val createCategory: Category = CategoryFactory.createCategory()
            Mockito.doReturn(createCategory).`when`(categoryService).findCategory(CATEGORY_SLUG)
            // When
            val getImageOfCategory = categoryController.getImageOfCategory(CATEGORY_SLUG)
            // Then
            Assertions.assertNotNull(getImageOfCategory)
        }
    }
}
