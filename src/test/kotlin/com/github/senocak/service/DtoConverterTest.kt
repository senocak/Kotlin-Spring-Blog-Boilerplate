package com.github.senocak.service

import com.github.senocak.controller.CategoryController
import com.github.senocak.controller.UserController
import com.github.senocak.domain.Post
import com.github.senocak.domain.Role
import com.github.senocak.domain.Comment
import com.github.senocak.domain.Category
import com.github.senocak.domain.User
import com.github.senocak.domain.dto.auth.RoleResponse
import com.github.senocak.domain.dto.category.CategoryDto
import com.github.senocak.domain.dto.comment.CommentDto
import com.github.senocak.domain.dto.post.PostDto
import com.github.senocak.domain.dto.user.UserResponse
import com.github.senocak.factory.CategoryFactory.createCategory
import com.github.senocak.factory.CommentFactory.createComment
import com.github.senocak.factory.PostFactory.createPost
import com.github.senocak.factory.UserFactory.createUser
import com.github.senocak.service.DtoConverter.convertPostEntityToPostsDto
import com.github.senocak.util.RoleName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@Tag("unit")
@ExtendWith(MockitoExtension::class)
@DisplayName("Unit Tests for DtoConverter")
class DtoConverterTest {
    @Test
    fun givenPost_whenConvertPostEntityToPostsDto_thenAssertResult() {
        // Given
        val post: Post = createPost()
        val category: Category = createCategory()
        val comment: Comment = createComment()
        post.categories = mutableListOf(category)
        post.comments = mutableListOf(comment)
        post.tags = listOf("lorem")
        // When
        val convertPostEntityToPostsDto: PostDto = convertPostEntityToPostsDto(post)
        // Then
        Assertions.assertEquals(post.title, convertPostEntityToPostsDto.title)
        Assertions.assertEquals(post.slug, convertPostEntityToPostsDto.slug)
        Assertions.assertEquals(post.body, convertPostEntityToPostsDto.body)
        Assertions.assertEquals(post.user!!.username, convertPostEntityToPostsDto.user!!.username)
        Assertions.assertEquals(1, convertPostEntityToPostsDto.categories!!.size)
        Assertions.assertEquals(1, convertPostEntityToPostsDto.comments!!.size)
        Assertions.assertEquals(1, convertPostEntityToPostsDto.tags!!.size)
        val firstCategory: Optional<CategoryDto> = convertPostEntityToPostsDto.categories!!.stream().findFirst()
        Assertions.assertTrue(firstCategory.isPresent)
        Assertions.assertEquals(category.name, firstCategory.get().name)
        val firstComment: Optional<CommentDto> = convertPostEntityToPostsDto.comments!!.stream().findFirst()
        Assertions.assertTrue(firstComment.isPresent)
        Assertions.assertEquals(comment.email, firstComment.get().email)
        val firstTag: Optional<String> = convertPostEntityToPostsDto.tags!!.stream().findFirst()
        Assertions.assertTrue(firstTag.isPresent)
        Assertions.assertEquals("lorem", firstTag.get())
    }

    @Test
    fun givenCategory_whenConvertEntityToDto_thenAssertResult() {
        // Given
        val category: Category = createCategory()
        val post: Post = createPost()
        category.posts = mutableListOf(post)
        // When
        val convertEntityToDto: CategoryDto = DtoConverter.convertEntityToDto(category)
        // Then
        Assertions.assertNotNull(convertEntityToDto.resourceId)
        Assertions.assertEquals(category.slug, convertEntityToDto.slug)
        Assertions.assertEquals(category.name, convertEntityToDto.name)
        Assertions.assertEquals(1, convertEntityToDto.postDto!!.size)
        val firstPost: Optional<PostDto> = convertEntityToDto.postDto!!.stream().findFirst()
        Assertions.assertTrue(firstPost.isPresent)
        Assertions.assertEquals(post.title, firstPost.get().title)
        Assertions.assertEquals(
            CategoryController.URL + "/" + category.slug + "/image",
            convertEntityToDto.image
        )
        Assertions.assertEquals(CategoryController.URL + "/" + category.slug, convertEntityToDto.resourceUrl)
    }

    @Test
    fun givenComment_whenConvertEntityToDto_thenAssertResult() {
        // Given
        val comment: Comment = createComment()
        // When
        val convertEntityToDto: CommentDto = DtoConverter.convertEntityToDto(comment)
        // Then
        Assertions.assertEquals(comment.name, convertEntityToDto.name)
        Assertions.assertEquals(comment.email, convertEntityToDto.email)
        Assertions.assertEquals(comment.body, convertEntityToDto.body)
        Assertions.assertNotNull(convertEntityToDto.createdAt)
        Assertions.assertTrue(convertEntityToDto.approved)
    }

    @Test
    fun givenUser_whenConvertEntityToDto_thenAssertResult() {
        // Given
        val user: User = createUser()
        // When
        val convertEntityToDto: UserResponse = DtoConverter.convertEntityToDto(user)
        // Then
        Assertions.assertEquals(user.name, convertEntityToDto.name)
        Assertions.assertEquals(user.email, convertEntityToDto.email)
        Assertions.assertEquals(user.username, convertEntityToDto.username)
        Assertions.assertEquals(UserController.URL + "/" + user.username, convertEntityToDto.resourceUrl)
    }

    @Test
    fun givenRole_whenConvertEntityToDto_thenAssertResult() {
        // Given
        val role = Role()
        role.name = RoleName.ROLE_USER
        // When
        val convertEntityToDto: RoleResponse = DtoConverter.convertEntityToDto(role)
        // Then
        Assertions.assertEquals(role.name, convertEntityToDto.name)
    }
}
