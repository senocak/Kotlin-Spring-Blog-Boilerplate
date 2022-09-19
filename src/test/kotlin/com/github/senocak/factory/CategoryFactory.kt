package com.github.senocak.factory

import com.github.senocak.TestConstants.CATEGORY_IMAGE
import com.github.senocak.TestConstants.CATEGORY_NAME
import com.github.senocak.TestConstants.CATEGORY_SLUG
import com.github.senocak.domain.Category
import java.util.UUID

object CategoryFactory {

    /**
     * Creates a new Category object.
     * @return Category object
     */
    fun createCategory(): Category {
        val category = Category()
        category.id = UUID.randomUUID().toString()
        category.name = CATEGORY_NAME
        category.image = CATEGORY_IMAGE
        category.slug = CATEGORY_SLUG
        category.posts = ArrayList()
        return category
    }
}
