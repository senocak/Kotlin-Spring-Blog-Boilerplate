package com.github.senocak.controller

import com.github.senocak.TestConstants
import com.github.senocak.domain.Category
import com.github.senocak.domain.Comment
import com.github.senocak.domain.Post
import com.github.senocak.domain.User
import com.github.senocak.domain.dto.comment.CommentRequestDto
import com.github.senocak.domain.dto.comment.CommentWrapperDto
import com.github.senocak.domain.dto.post.CreatePostDto
import com.github.senocak.domain.dto.post.PostUpdateDto
import com.github.senocak.domain.dto.post.PostWrapperDto
import com.github.senocak.domain.dto.post.PostsDto
import com.github.senocak.exception.ServerException
import com.github.senocak.factory.CategoryFactory.createCategory
import com.github.senocak.factory.CommentFactory
import com.github.senocak.factory.PostFactory
import com.github.senocak.factory.UserFactory.createUser
import com.github.senocak.service.CategoryService
import com.github.senocak.service.CommentService
import com.github.senocak.service.PostService
import com.github.senocak.service.UserService
import com.github.senocak.util.AppConstants.toSlug
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
import org.mockito.kotlin.any

@Tag("unit")
@ExtendWith(MockitoExtension::class)
@DisplayName("Unit Tests for PostsController")
class PostsControllerTest {
    private val categoryService = Mockito.mock(CategoryService::class.java)
    private val userService = Mockito.mock(UserService::class.java)
    private val postService = Mockito.mock(PostService::class.java)
    private val commentService = Mockito.mock(CommentService::class.java)
    private val postsController = PostsController(categoryService, userService, postService, commentService)
    private val bindingResult = Mockito.mock(BindingResult::class.java)

    @Nested
    internal inner class CreateTest {
        private val createPostDto: CreatePostDto = CreatePostDto()
        private val POST_NAME = "lorem"

        @Test
        fun givenExistPost_whenCreate_thenThrowServerException() {
            // Given
            createPostDto.title = POST_NAME
            val post: Post = PostFactory.createPost()
            Mockito.doReturn(post).`when`(postService).findPostBySlugOrId(toSlug(createPostDto.title!!))
            // When
            val closureToTest = Executable { postsController.create(createPostDto, bindingResult) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenCreate_thenReturn201() {
            // Given
            createPostDto.title = POST_NAME
            val categoryList: MutableList<String> = ArrayList()
            val CATEGORY_NAME = "lorem"
            categoryList.add(CATEGORY_NAME)
            createPostDto.category = categoryList
            createPostDto.tags = ArrayList()
            val category: Category = createCategory()
            Mockito.doReturn(category).`when`(categoryService).findCategory(CATEGORY_NAME)
            Mockito.doReturn(null).`when`(postService).findPostBySlugOrId(toSlug(createPostDto.title!!))
            val post: Post = PostFactory.createPost()
            Mockito.doReturn(post).`when`(postService).persist(any<Post>())
            // When
            val getAll: ResponseEntity<PostWrapperDto> = postsController.create(createPostDto, bindingResult)
            // Then
            Assertions.assertNotNull(getAll)
            Assertions.assertNotNull(getAll.statusCode)
            Assertions.assertEquals(HttpStatus.CREATED, getAll.statusCode)
            Assertions.assertNotNull(getAll.body)
            Assertions.assertNotNull(getAll.body!!.postDto)
            Assertions.assertNotNull(getAll.body!!.postDto!!.resourceId)
            Assertions.assertEquals(post.title, getAll.body!!.postDto!!.title)
            Assertions.assertEquals(post.slug, getAll.body!!.postDto!!.slug)
            Assertions.assertEquals(post.body, getAll.body!!.postDto!!.body)
            Assertions.assertEquals(post.user!!.username, getAll.body!!.postDto!!.user!!.username)
            Assertions.assertEquals(PostsController.URL + "/" + post.slug,
                getAll.body!!.postDto!!.resourceUrl)
        }
    }

    @Nested
    internal inner class CreateCommentTest {
        private val commentRequestDto: CommentRequestDto = CommentRequestDto()
        private val POST_NAME = "lorem"

        @Test
        @Throws(ServerException::class)
        fun given_whenCreateComment_thenReturn201() {
            // Given
            commentRequestDto.name = TestConstants.COMMENT_TITLE
            commentRequestDto.body = TestConstants.COMMENT_BODY
            commentRequestDto.email = TestConstants.COMMENT_EMAIL
            val post: Post = PostFactory.createPost()
            Mockito.doReturn(post).`when`(postService).findPostBySlugOrId(POST_NAME)
            val comment: Comment = CommentFactory.createComment()
            Mockito.doReturn(comment).`when`(commentService).persist(any<Comment>())
            // When
            val getAll: ResponseEntity<CommentWrapperDto> =
                postsController.createComment(POST_NAME, commentRequestDto, bindingResult)
            // Then
            Assertions.assertNotNull(getAll)
            Assertions.assertNotNull(getAll.statusCode)
            Assertions.assertEquals(HttpStatus.CREATED, getAll.statusCode)
            Assertions.assertNotNull(getAll.body)
            Assertions.assertNotNull(getAll.body!!.commentDto)
            Assertions.assertEquals(TestConstants.COMMENT_TITLE, getAll.body!!.commentDto!!.name)
            Assertions.assertEquals(TestConstants.COMMENT_EMAIL, getAll.body!!.commentDto!!.email)
            Assertions.assertEquals(TestConstants.COMMENT_BODY, getAll.body!!.commentDto!!.body)
            Assertions.assertTrue(getAll.body!!.commentDto!!.approved)
        }
    }

    @Nested
    internal inner class UpdateCommentVisibilityTest {
        private val POST_NAME = "lorem"
        private val COMMENT_NAME = "lorem"

        @Test
        fun givenInvalidPost_whenUpdateComment_thenThrowServerException() {
            // Given
            Mockito.doReturn(null).`when`(postService).findPostBySlugOrId(POST_NAME)
            // When
            val closureToTest =
                Executable { postsController.updateCommentVisibility(POST_NAME, COMMENT_NAME, "false") }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        fun givenInvalidComment_whenUpdateComment_thenThrowServerException() {
            // Given
            val post: Post = PostFactory.createPost()
            Mockito.doReturn(post).`when`(postService).findPostBySlugOrId(POST_NAME)
            Mockito.doReturn(null).`when`(commentService).findById(COMMENT_NAME)
            // When
            val closureToTest =
                Executable { postsController.updateCommentVisibility(POST_NAME, COMMENT_NAME, "false") }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        fun givenPostNotBelongToComment_whenUpdateComment_thenThrowServerException() {
            // Given
            val post: Post = PostFactory.createPost()
            Mockito.doReturn(post).`when`(postService).findPostBySlugOrId(POST_NAME)
            val comment: Comment = CommentFactory.createComment()
            comment.post!!.slug = "invalid"
            Mockito.doReturn(comment).`when`(commentService).findById(COMMENT_NAME)
            // When
            val closureToTest =
                Executable { postsController.updateCommentVisibility(POST_NAME, COMMENT_NAME, "false") }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenUpdateComment_thenReturn200() {
            // Given
            val post: Post = PostFactory.createPost()
            Mockito.doReturn(post).`when`(postService).findPostBySlugOrId(POST_NAME)
            val comment: Comment = CommentFactory.createComment()
            comment.post = post
            Mockito.doReturn(comment).`when`(commentService).findById(COMMENT_NAME)
            val commentUpdated: Comment = CommentFactory.createComment()
            Mockito.doReturn(commentUpdated).`when`(commentService).persist(any<Comment>())
            // When
            val getAll: ResponseEntity<CommentWrapperDto> =
                postsController.updateCommentVisibility(POST_NAME, COMMENT_NAME, "false")
            // Then
            Assertions.assertNotNull(getAll)
            Assertions.assertNotNull(getAll.statusCode)
            Assertions.assertEquals(HttpStatus.OK, getAll.statusCode)
            Assertions.assertNotNull(getAll.body)
            Assertions.assertNotNull(getAll.body!!.commentDto)
            Assertions.assertEquals(commentUpdated.name, getAll.body!!.commentDto!!.name)
            Assertions.assertEquals(commentUpdated.email, getAll.body!!.commentDto!!.email)
            Assertions.assertEquals(commentUpdated.body, getAll.body!!.commentDto!!.body)
            Assertions.assertTrue(getAll.body!!.commentDto!!.approved)
        }
    }

    @Nested
    internal inner class UpdatePostTest {
        private val POST_NAME = "lorem"
        private val postUpdateDto: PostUpdateDto = PostUpdateDto()

        @Test
        fun givenInvalidPost_whenUpdatePost_thenThrowServerException() {
            // Given
            Mockito.doReturn(null).`when`(postService).findPostBySlugOrId(POST_NAME)
            // When
            val closureToTest =
                Executable { postsController.updatePost(POST_NAME, postUpdateDto, bindingResult) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun givenPostNotBelongToUser_whenUpdatePost_thenThrowServerException() {
            // Given
            val post: Post = PostFactory.createPost()
            post.user!!.email = "fakemail"
            val user: User = createUser()
            user.roles = HashSet()
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            Mockito.doReturn(post).`when`(postService).findPostBySlugOrId(POST_NAME)
            // When
            val closureToTest =
                Executable { postsController.updatePost(POST_NAME, postUpdateDto, bindingResult) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenUpdatePost_thenReturn200() {
            // Given
            val post: Post = PostFactory.createPost()
            val user: User = createUser()
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            Mockito.doReturn(post).`when`(postService).findPostBySlugOrId(POST_NAME)
            postUpdateDto.title = TestConstants.POST_TITLE
            postUpdateDto.body = TestConstants.POST_BODY
            postUpdateDto.tags = ArrayList()
            val categoryList: MutableList<String> = ArrayList()
            categoryList.add(TestConstants.CATEGORY_NAME)
            postUpdateDto.category = categoryList
            val category: Category = createCategory()
            Mockito.doReturn(category).`when`(categoryService).findCategory(categoryList[0])
            val postUpdated: Post = PostFactory.createPost()
            Mockito.doReturn(postUpdated).`when`(postService).persist(any<Post>())
            // When
            val updatePost: ResponseEntity<PostWrapperDto> = postsController.updatePost(POST_NAME, postUpdateDto, bindingResult)
            // Then
            Assertions.assertNotNull(updatePost)
            Assertions.assertEquals(HttpStatus.OK, updatePost.statusCode)
            Assertions.assertNotNull(updatePost.body)
            Assertions.assertNotNull(updatePost.body!!.postDto)
            Assertions.assertNotNull(updatePost.body!!.postDto!!.resourceId)
            Assertions.assertEquals(post.title, updatePost.body!!.postDto!!.title)
            Assertions.assertEquals(post.slug, updatePost.body!!.postDto!!.slug)
            Assertions.assertEquals(post.body, updatePost.body!!.postDto!!.body)
            Assertions.assertEquals(post.user!!.username,
                updatePost.body!!.postDto!!.user!!.username)
            Assertions.assertNull(updatePost.body!!.postDto!!.categories)
            Assertions.assertNotNull(updatePost.body!!.postDto!!.updatedAt)
            Assertions.assertEquals(PostsController.URL + "/" + post.slug,
                updatePost.body!!.postDto!!.resourceUrl)
        }
    }

    @Nested
    internal inner class DeletePostTest {
        private val POST_NAME = "lorem"

        @Test
        fun givenInvalidPost_whenDeletePost_thenThrowServerException() {
            // Given
            Mockito.doReturn(null).`when`(postService).findPostBySlugOrId(POST_NAME)
            // When
            val closureToTest = Executable { postsController.deletePost(POST_NAME) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun givenPostNotBelongToUser_whenDeletePost_thenThrowServerException() {
            // Given
            val post: Post = PostFactory.createPost()
            post.user!!.email = "fakemail"
            val user: User = createUser()
            user.roles = HashSet()
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            Mockito.doReturn(post).`when`(postService).findPostBySlugOrId(POST_NAME)
            // When
            val closureToTest = Executable { postsController.deletePost(POST_NAME) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenDeletePost_thenReturn200() {
            // Given
            val user: User = createUser()
            Mockito.doReturn(user).`when`(userService).loggedInUser()
            val post: Post = PostFactory.createPost()
            Mockito.doReturn(post).`when`(postService).findPostBySlugOrId(POST_NAME)
            // When
            val updatePost = postsController.deletePost(POST_NAME)
            // Then
            Assertions.assertNotNull(updatePost)
            Assertions.assertEquals(HttpStatus.NO_CONTENT, updatePost.statusCode)
        }
    }

    @Nested
    internal inner class GetAllTest {
        @Test
        @Throws(ServerException::class)
        fun given_whenGetAll_thenReturn200() {
            // Given
            val user: User = createUser()
            Mockito.doReturn(user).`when`(userService).findByUsername(TestConstants.USER_USERNAME)
            val postList: MutableList<Post> = ArrayList<Post>()
            val post: Post = PostFactory.createPost()
            postList.add(post)
            val posts: Page<Post> = PageImpl(postList)
            Mockito.doReturn(posts).`when`(postService).getAllByUser(
                user, null, 0, 50
            )
            // When
            val getAll: ResponseEntity<PostsDto> = postsController.getAll(
                0, 50,
                TestConstants.USER_USERNAME, ""
            )
            // Then
            Assertions.assertNotNull(getAll)
            Assertions.assertNotNull(getAll.statusCode)
            Assertions.assertEquals(HttpStatus.OK, getAll.statusCode)
            Assertions.assertNotNull(getAll.body)
            Assertions.assertEquals(1, getAll.body!!.total)
            Assertions.assertEquals(0, getAll.body!!.next)
            Assertions.assertEquals(PostsController.URL, getAll.body!!.resourceUrl)
            Assertions.assertNotNull(getAll.body!!.postDto)
            Assertions.assertEquals(1, getAll.body!!.postDto.size)
            Assertions.assertEquals(post.title, getAll.body!!.postDto[0].title)
            Assertions.assertNotNull(getAll.body!!.postDto[0].resourceId)
            Assertions.assertEquals(post.slug, getAll.body!!.postDto[0].slug)
            Assertions.assertEquals(post.body, getAll.body!!.postDto[0].body)
            Assertions.assertEquals(PostsController.URL + "/" + post.slug,
                getAll.body!!.postDto[0].resourceUrl)
        }
    }

    @Nested
    internal inner class GetSingleTest {
        @Test
        fun givenInvalidPost_whenGetSingle_thenThrowServerException() {
            // Given
            val post = "post"
            Mockito.doReturn(null).`when`(postService).findPostBySlugOrId(post)
            // When
            val closureToTest = Executable { postsController.getSingle(post) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenGetSingle_thenReturn200() {
            // Given
            val postName = "post"
            val post: Post = PostFactory.createPost()
            Mockito.doReturn(post).`when`(postService).findPostBySlugOrId(postName)
            // When
            val getAll: ResponseEntity<PostWrapperDto> = postsController.getSingle(postName)
            // Then
            Assertions.assertNotNull(getAll)
            Assertions.assertNotNull(getAll.statusCode)
            Assertions.assertEquals(HttpStatus.OK, getAll.statusCode)
            Assertions.assertNotNull(getAll.body)
            Assertions.assertNotNull(getAll.body!!.postDto)
            Assertions.assertNotNull(getAll.body!!.postDto!!.resourceId)
            Assertions.assertEquals(post.title, getAll.body!!.postDto!!.title)
            Assertions.assertEquals(post.slug, getAll.body!!.postDto!!.slug)
            Assertions.assertEquals(post.body, getAll.body!!.postDto!!.body)
            Assertions.assertEquals(post.user!!.username, getAll.body!!.postDto!!.user!!.username)
            Assertions.assertEquals(0, getAll.body!!.postDto!!.tags!!.size)
            Assertions.assertEquals(PostsController.URL + "/" + post.slug,
                getAll.body!!.postDto!!.resourceUrl)
        }
    }
}