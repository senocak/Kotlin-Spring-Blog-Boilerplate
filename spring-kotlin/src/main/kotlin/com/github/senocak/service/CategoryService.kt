package com.github.senocak.service

import com.github.senocak.domain.Category
import com.github.senocak.domain.dto.category.CategoryCreateRequestDto
import com.github.senocak.domain.dto.category.CategoryUpdateRequestDto
import com.github.senocak.exception.ServerException
import com.github.senocak.repository.CategoryRepository
import com.github.senocak.util.AppConstants
import com.github.senocak.util.OmaErrorMessageType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.Objects

@Service
class CategoryService(private val categoryRepository: CategoryRepository) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Create new category
     * @param categoryCreateRequestDto -- CategoryCreateRequestDto object to be created
     * @return -- Category entity that is created
     * @throws ServerException -- ServerException if category name or slug is already exist
     */
    @Caching(
        put = [
            CachePut(value = arrayOf(AppConstants.CACHE_CATEGORY), key = "#result.id"),
            CachePut(value = arrayOf(AppConstants.CACHE_CATEGORY), key = "#result.slug")
        ]
    )
    @Throws(ServerException::class)
    fun createCategory(categoryCreateRequestDto: CategoryCreateRequestDto): Category {
        var category: Category? = null
        try {
            category = findCategory(AppConstants.toSlug(categoryCreateRequestDto.name.toString()))
        } catch (serverException: ServerException) {
            log.debug("Caught ServerException, Post not exist in db")
        }
        if (category != null) {
            val error = "Category " + category.name + " already exist"
            log.error(error)
            throw ServerException(OmaErrorMessageType.BASIC_INVALID_INPUT, arrayOf(error), HttpStatus.CONFLICT)
        }
        category = Category()
        category.name = categoryCreateRequestDto.name.toString()
        if (Objects.nonNull(categoryCreateRequestDto.image) && categoryCreateRequestDto.image.toString() != "")
            category.image = categoryCreateRequestDto.image
        val saveCategory = saveCategory(category)
        return saveCategory
    }

    /**
     * @param nextPage -- next page variable to filter
     * @param maxNumber -- max page to retrieve from db
     * @return -- post objects that has retrieved by page
     */
    fun getAll(nextPage: Int, maxNumber: Int): Page<Category?>? {
        val paging: Pageable = PageRequest.of(nextPage, maxNumber)
        return categoryRepository.findAll(paging)
    }

    /**
     * @param idOrSlug -- slug or identifier of Category entity
     * @return -- Category entity that is retrieved from db
     * @throws ServerException -- if Category is not found
     */
    @Cacheable(value = [AppConstants.CACHE_CATEGORY], key = "#idOrSlug", unless = "#result == null")
    @Throws(ServerException::class)
    fun findCategory(idOrSlug: String): Category {
        val category = categoryRepository.findByIdOrSlug(idOrSlug)
        if (Objects.isNull(category) || !category.isPresent) {
            log.error("Category is not found.")
            throw ServerException(OmaErrorMessageType.NOT_FOUND, arrayOf("Category: $idOrSlug"), HttpStatus.NOT_FOUND)
        }
        return category.get()
    }

    /**
     * @param category -- Category entity to be wanted to persist
     * @return -- Persisted Category entity
     */
    @Caching(
        evict = [CacheEvict(value = arrayOf(AppConstants.CACHE_CATEGORY), key = "#category.slug")],
        put = [
            CachePut(value = arrayOf(AppConstants.CACHE_CATEGORY), key = "#result.id"),
            CachePut(value = arrayOf(AppConstants.CACHE_CATEGORY), key = "#result.slug")
        ]
    )
    fun updateCategory(category: Category, categoryUpdateRequestDto: CategoryUpdateRequestDto): Category {
        val categoryName: String? = categoryUpdateRequestDto.name
        if (Objects.nonNull(categoryName) && categoryName != "")
            category.name = categoryName
        val categoryImage: String? = categoryUpdateRequestDto.image
        if (Objects.nonNull(categoryImage) && categoryImage != "")
            category.image = categoryImage
        return saveCategory(category)
    }

    /**
     * @param category -- Category entity to be wanted to delete
     */
    @Caching(
        evict = [
            CacheEvict(value = arrayOf(AppConstants.CACHE_CATEGORY), key = "#category.id"),
            CacheEvict(value = arrayOf(AppConstants.CACHE_CATEGORY), key = "#category.slug")
        ]
    )
    fun deleteCategory(category: Category) {
        categoryRepository.delete(category)
    }

    /**
     * Update category
     * @param category -- Category entity to be wanted to update
     * @return -- Updated Category entity
     */
    private fun saveCategory(category: Category): Category {
        return categoryRepository.save(category)
    }
}