package com.github.senocak.controller

import com.github.senocak.domain.Category
import com.github.senocak.domain.ExceptionDto
import com.github.senocak.domain.dto.category.CategoriesDto
import com.github.senocak.domain.dto.category.CategoryCreateRequestDto
import com.github.senocak.domain.dto.category.CategoryDto
import com.github.senocak.domain.dto.category.CategoryWrapperDto
import com.github.senocak.domain.dto.category.CategoryUpdateRequestDto
import com.github.senocak.exception.ServerException
import com.github.senocak.security.Authorize
import com.github.senocak.service.CategoryService
import com.github.senocak.service.DtoConverter
import com.github.senocak.util.AppConstants
import com.github.senocak.util.AppConstants.ADMIN
import com.github.senocak.util.AppConstants.DEFAULT_PAGE_NUMBER
import com.github.senocak.util.AppConstants.DEFAULT_PAGE_SIZE
import com.github.senocak.util.AppConstants.securitySchemeName
import com.github.senocak.util.OmaErrorMessageType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.Objects
import java.util.stream.Collectors
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import kotlin.ByteArray
import kotlin.Int
import kotlin.String
import kotlin.arrayOf

@Validated
@RestController
@RequestMapping(CategoryController.URL)
@Tag(name = "Category", description = "Category API")
class CategoryController(private val categoryService: CategoryService) {

    @Authorize(roles = [ADMIN])
    @PostMapping
    @Operation(
        summary = "Create Category",
        tags = ["Category"],
        responses = [
            ApiResponse(responseCode = "201", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = CategoryWrapperDto::class)))),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN])]
    )
    fun create(
        @Parameter(description = "Request body to create", required = true)
            @Validated @RequestBody categoryCreateRequestDto: CategoryCreateRequestDto,
        resultOfValidation: BindingResult
    ): ResponseEntity<CategoryWrapperDto> {
        validate(resultOfValidation)
        val category: Category = categoryService.createCategory(categoryCreateRequestDto)
        val categoryDto: CategoryDto = DtoConverter.convertEntityToDto(category)
        categoryDto.resourceUrl = URL + "/" + category.slug
        val categoryWrapperDto = CategoryWrapperDto()
        categoryWrapperDto.categoryDto = categoryDto
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryWrapperDto)
    }

    @GetMapping
    @Operation(
        summary = "Get All Categories",
        tags = ["Category"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = CategoriesDto::class)))),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ]
    )
    fun getAll(
        @Parameter(description = "Number of resources that is requested.") @RequestParam(value = "next", defaultValue = DEFAULT_PAGE_NUMBER) @Min(0) @Max(99) nextPage: Int,
        @Parameter(description = "Pointer for the next page to retrieve.") @RequestParam(value = "max", defaultValue = DEFAULT_PAGE_SIZE) @Min(0) @Max(99) maxNumber: Int,
        @Parameter(description = "Boolean value to display posts") @RequestParam(value = "posts", defaultValue = "false") posts: String
    ): ResponseEntity<CategoriesDto> {
        val categories = categoryService.getAll(nextPage, maxNumber)
        val dtos: List<CategoryDto>? = categories!!.content.stream()
            .map { c ->
                val dto = DtoConverter.convertEntityToDto(c!!)
                if (posts != "true")
                    dto.postDto = null
                if (dto.postDto != null && dto.postDto!!.isNotEmpty())
                    dto.postDto!!.forEach { p -> p.categories = null }
                dto.resourceUrl = URL + "/" + c.slug
                dto
            }
            .collect(Collectors.toList())
        val categoriesDto =
            CategoriesDto(dtos, categories.totalElements, if (categories.hasNext()) (nextPage + 1).toLong() else 0, URL)
        return ResponseEntity.ok(categoriesDto)
    }

    @PatchMapping("/{slug}")
    @Authorize(roles = [ADMIN])
    @Operation(
        summary = "Update Category",
        tags = ["Category"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = CategoryWrapperDto::class)))),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN])]
    )
    @Throws(ServerException::class)
    fun update(
        @Parameter(description = "Identifier of the category", required = true)
            @PathVariable slug: String,
        @Parameter(description = "Request body to update", required = true)
            @Validated @RequestBody categoryUpdateRequestDto: CategoryUpdateRequestDto?,
        resultOfValidation: BindingResult
    ): ResponseEntity<CategoryWrapperDto>? {
        validate(resultOfValidation)
        val category = categoryService.findCategory(AppConstants.toSlug(slug))
        val savedCategory = categoryService.updateCategory(category, categoryUpdateRequestDto!!)
        val categoryDto = DtoConverter.convertEntityToDto(savedCategory)
        categoryDto.resourceUrl = "$URL/${savedCategory.slug}"
        return ResponseEntity.ok(CategoryWrapperDto(categoryDto))
    }

    @DeleteMapping(value = ["/{slug}"])
    @Authorize(roles = [ADMIN])
    @Operation(
        summary = "Delete Category",
        tags = ["Category"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation"),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN])]
    )
    @Throws(ServerException::class)
    fun delete(
        @Parameter(description = "Identifier of the category", required = true) @PathVariable slug: String?
    ): ResponseEntity<Void?>? {
        val category = categoryService.findCategory(AppConstants.toSlug(slug!!))
        categoryService.deleteCategory(category)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{slug}")
    @Operation(
        summary = "Get Single Category",
        tags = ["Category"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = CategoriesDto::class)))),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ]
    )
    @Throws(ServerException::class)
    fun getSingle(
        @Parameter(description = "Category identifier", required = true) @PathVariable slug: String,
        @Parameter(description = "Boolean value to display posts", required = true) @RequestParam(value = "posts", defaultValue = "false") posts: String
    ): ResponseEntity<CategoryWrapperDto>? {
        val category = categoryService.findCategory(slug)
        if (posts != "true")
            category.posts = null
        val dto = DtoConverter.convertEntityToDto(category)
        dto.resourceUrl = "$URL/$slug"
        return ResponseEntity.ok(CategoryWrapperDto(dto))
    }

    @GetMapping(value = ["/{slug}/image"], produces = [MediaType.IMAGE_PNG_VALUE])
    @Operation(
        summary = "Get Image Of Category",
        tags = ["Category"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = CategoriesDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ]
    )
    @Throws(ServerException::class)
    fun getImageOfCategory(@Parameter(description = "Identifier of the category", required = true) @PathVariable slug: String): ByteArray? {
        val category = categoryService.findCategory(slug)
        val image: String? = category.image
        if (Objects.isNull(image)) {
            throw ServerException(OmaErrorMessageType.MANDATORY_INPUT_MISSING,
                arrayOf(category.name, "Image"), HttpStatus.BAD_REQUEST) }
        return image!!.toByteArray(Charsets.UTF_8)
    }

    companion object {
        const val URL = "/api/v1/categories"
    }
}