package com.github.senocak.controller

import com.github.senocak.domain.Category
import com.github.senocak.domain.Comment
import com.github.senocak.domain.ExceptionDto
import com.github.senocak.domain.Post
import com.github.senocak.domain.User
import com.github.senocak.domain.dto.comment.CommentDto
import com.github.senocak.domain.dto.comment.CommentRequestDto
import com.github.senocak.domain.dto.comment.CommentWrapperDto
import com.github.senocak.domain.dto.post.PostsDto
import com.github.senocak.domain.dto.post.PostWrapperDto
import com.github.senocak.domain.dto.post.PostUpdateDto
import com.github.senocak.domain.dto.post.CreatePostDto
import com.github.senocak.domain.dto.post.PostDto
import com.github.senocak.exception.ServerException
import com.github.senocak.security.Authorize
import com.github.senocak.service.CategoryService
import com.github.senocak.service.UserService
import com.github.senocak.service.PostService
import com.github.senocak.service.CommentService
import com.github.senocak.service.DtoConverter
import com.github.senocak.util.AppConstants
import com.github.senocak.util.AppConstants.ADMIN
import com.github.senocak.util.AppConstants.USER
import com.github.senocak.util.AppConstants.securitySchemeName
import com.github.senocak.util.AppConstants.toSlug
import com.github.senocak.util.OmaErrorMessageType
import com.github.senocak.util.RoleName
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.StringUtils
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.Boolean
import java.util.Date
import java.util.Objects
import java.util.stream.Collectors
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import kotlin.Int
import kotlin.String
import kotlin.arrayOf
import kotlin.collections.ArrayList

@Validated
@RestController
@RequestMapping(PostsController.URL)
@Tag(name = "Post", description = "Post API")
class PostsController(
    private val categoryService: CategoryService,
    private val userService: UserService,
    private val postService: PostService,
    private val commentService: CommentService
): BaseController() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping
    @Authorize(roles = [ADMIN, USER])
    @Operation(
        summary = "Create Post",
        tags = ["Post"],
        responses = [
            ApiResponse(responseCode = "201", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = PostWrapperDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @Throws(ServerException::class)
    fun create(
        @Parameter(description = "Request body to create", required = true) @Validated @RequestBody createPostDto: CreatePostDto,
        resultOfValidation: BindingResult
    ): ResponseEntity<PostWrapperDto> {
        validate(resultOfValidation)
        var postByName: Post? = null
        try {
            postByName = findPostBySlugOrId(toSlug(createPostDto.title!!))
        } catch (serverException: ServerException) {
            log.warn("Caught ServerException, Post not exist in db")
        }
        if (Objects.nonNull(postByName)) {
            log.error("Post exist.")
            throw ServerException(OmaErrorMessageType.BASIC_INVALID_INPUT,
                arrayOf("Post already exist"), HttpStatus.CONFLICT)
        }
        val categories: MutableList<Category> = ArrayList()
        if (Objects.nonNull(createPostDto.category))
            for (cat in createPostDto.category!!) {
                val category: Category = categoryService.findCategory(cat)
                if (!categories.contains(category))
                    categories.add(category)
            }
        val post = Post()
        post.title = createPostDto.title
        post.body = createPostDto.body
        post.categories = categories
        post.user = userFromContext
        var tags: List<String>? = ArrayList()
        if (Objects.nonNull(createPostDto.tags))
            tags = createPostDto.tags!!
        post.tags = tags
        post.createdAt = Date()
        val savedPost: Post = postService.persist(post)
        val postDto: PostDto = DtoConverter.convertPostEntityToPostsDto(savedPost)
        postDto.resourceUrl = URL + "/" + postDto.slug
        return ResponseEntity.status(HttpStatus.CREATED).body(PostWrapperDto(postDto))
    }

    @PostMapping(value = ["/{slug}/comment"])
    @Operation(
        summary = "Add Comment To Post",
        tags = ["Post"],
        responses = [
            ApiResponse(responseCode = "201", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = CommentWrapperDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ]
    )
    @Throws(ServerException::class)
    fun createComment(
        @Parameter(description = "Identifier of the post", required = true) @PathVariable slug: String,
        @Parameter(description = "Request body to create comment", required = true)
            @Validated @RequestBody commentRequest: CommentRequestDto,
        resultOfValidation: BindingResult
    ): ResponseEntity<CommentWrapperDto> {
        validate(resultOfValidation)
        val post: Post? = findPostBySlugOrId(slug)
        val comment = Comment()
        comment.name = commentRequest.name
        comment.email = commentRequest.email
        comment.body = commentRequest.body
        comment.post = post
        val createdComment: Comment = commentService.persist(comment)
        log.info("Comment is created as {}", createdComment)
        val convertEntityToDto: CommentDto = DtoConverter.convertEntityToDto(createdComment)
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentWrapperDto(convertEntityToDto))
    }

    @PatchMapping(value = ["/{slug}/comment/{commentId}"])
    @Authorize(roles = [ADMIN])
    @Operation(
        summary = "Change comment visibility",
        tags = ["Post"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = CommentWrapperDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN])]
    )
    @Throws(ServerException::class)
    fun updateCommentVisibility(
        @Parameter(description = "Identifier of the post", required = true) @PathVariable slug: String,
        @Parameter(description = "Identifier of the comment", required = true) @PathVariable commentId: String,
        @Parameter(description = "A boolean type to update the comment", required = true) @RequestParam(value = "approve") approve: String?
    ): ResponseEntity<CommentWrapperDto> {
        val post: Post? = findPostBySlugOrId(slug)
        val comment: Comment = findComment(commentId)
        if (!comment.post!!.slug.equals(post!!.slug)) {
            log.error("Comment does not belong the post")
            throw ServerException(OmaErrorMessageType.BASIC_INVALID_INPUT,
                arrayOf("Comment does not belong the post"), HttpStatus.BAD_REQUEST)
        }
        comment.approved = Boolean.parseBoolean(approve)
        val commentUpdated: Comment = commentService.persist(comment)
        log.info("Comment: {} is updated as {}", commentId, commentUpdated)
        val convertEntityToDto: CommentDto = DtoConverter.convertEntityToDto(commentUpdated)
        return ResponseEntity.ok(CommentWrapperDto(convertEntityToDto))
    }

    @PatchMapping("/{slug}")
    @Authorize(roles = [ADMIN, USER])
    @Operation(
        summary = "Update Post",
        tags = ["Post"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = PostWrapperDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @Throws(ServerException::class)
    fun updatePost(
        @Parameter(description = "Identifier of the post", required = true) @PathVariable slug: String,
        @Parameter(description = "Request body to update the post", required = true)
            @Validated @RequestBody postUpdateDto: PostUpdateDto,
        resultOfValidation: BindingResult
    ): ResponseEntity<PostWrapperDto> {
        validate(resultOfValidation)
        val post: Post? = findPostBySlugOrId(slug)
        checkPostBelongToUser(post!!)
        if (Objects.nonNull(postUpdateDto.title)) post.title = postUpdateDto.title
        if (Objects.nonNull(postUpdateDto.body)) post.body = postUpdateDto.body
        if (Objects.nonNull(postUpdateDto.category)) {
            val categories: MutableList<Category> = ArrayList()
            for (cat in postUpdateDto.category!!) {
                val findCategory: Category = categoryService.findCategory(cat)
                categories.add(findCategory)
            }
            post.categories = categories
        }
        if (postUpdateDto.tags != null) post.tags = postUpdateDto.tags
        val updatePost: Post = postService.persist(post)
        val postDto: PostDto = DtoConverter.convertPostEntityToPostsDto(updatePost)
        postDto.resourceUrl = URL + "/" + postDto.slug
        return ResponseEntity.ok(PostWrapperDto(postDto))
    }

    @DeleteMapping(value = ["/{slug}"])
    @Authorize(roles = [ADMIN, USER])
    @Operation(
        summary = "Delete Post",
        tags = ["Post"],
        responses = [
            ApiResponse(responseCode = "204", description = "successful operation"),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @Throws(ServerException::class)
    fun deletePost(
        @Parameter(description = "Identifier of the post", required = true) @PathVariable slug: String
    ): ResponseEntity<Void> {
        val post: Post? = findPostBySlugOrId(slug)
        checkPostBelongToUser(post!!)
        postService.delete(post)
        log.info("Post: {} is deleted", post.title)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    @Operation(
        summary = "Get All Post",
        tags = ["Post"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = PostsDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ]
    )
    @Throws(ServerException::class)
    fun getAll(
        @Parameter(description = "Number of resources that is requested.", required = true)
            @RequestParam(value = "next", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) @Max(99) nextPage: Int,
        @Parameter(description = "Pointer for the next page to retrieve.", required = true)
            @RequestParam(value = "max", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) @Max(99) maxNumber: Int,
        @Parameter(description = "Username to filter", required = true)
            @RequestParam(value = "username", required = false) username: String?,
        @Parameter(description = "Category slug to filter", required = true)
            @RequestParam(value = "category", required = false) category: String?
    ): ResponseEntity<PostsDto> {
        var user: User? = null
        if (Objects.nonNull(username) && StringUtils.hasLength(username))
            user = findUser(username!!)
        var cat: Category? = null
        if (Objects.nonNull(category) && StringUtils.hasLength(category))
            cat = categoryService.findCategory(category!!)
        val posts: Page<Post> = postService.getAllByUser(user, cat, nextPage, maxNumber)
        val dtos: List<PostDto> = posts.content
            .stream()
            .map { p: Post ->
                val postDto: PostDto = DtoConverter.convertPostEntityToPostsDto(p)
                postDto.resourceUrl = URL + "/" + p.slug
                postDto
            }
            .collect(Collectors.toList())
        val postsDto = PostsDto(dtos, posts.totalElements, if (posts.hasNext()) (nextPage + 1).toLong() else 0, URL)
        return ResponseEntity.ok(postsDto)
    }

    @GetMapping(value = ["/{slug}"])
    @Operation(
        summary = "Get Single Post",
        tags = ["Post"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = PostWrapperDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ]
    )
    @Throws(ServerException::class)
    fun getSingle(
        @Parameter(description = "Post identifier", required = true) @PathVariable slug: String
    ): ResponseEntity<PostWrapperDto> {
        val post: Post? = findPostBySlugOrId(slug)
        val postDto: PostDto = DtoConverter.convertPostEntityToPostsDto(post!!)
        postDto.resourceUrl = URL + "/" + post.slug
        return ResponseEntity.ok(PostWrapperDto(postDto))
    }

    /**
     * @param post -- parameter that needs to be checked and verified that user has access to it
     * @throws ServerException -- throws ServerException
     */
    @Throws(ServerException::class)
    private fun checkPostBelongToUser(post: Post) {
        val user: User? = userFromContext
        if (user!!.roles.stream().noneMatch { role -> role.name === RoleName.ROLE_ADMIN }
            && post.user!!.email != user.email) {
            log.error("Post user:{} does not match with jwt user: {}", post.user!!.email, user.email)
            throw ServerException(OmaErrorMessageType.UNAUTHORIZED,
                arrayOf("This user does not have the right permission for this operation"), HttpStatus.UNAUTHORIZED)
        }
    }

    /**
     * @return -- User object retrieved from security context
     * @throws ServerException -- if user not found in context based on jwt token
     */
    @get:Throws(ServerException::class)
    private val userFromContext: User?
        get() = userService.loggedInUser()

    /**
     * @param slugOrId -- slug or identifier of Post entity
     * @return -- Post entity that is retrieved from db
     * @throws ServerException -- if post is not found
     */
    @Throws(ServerException::class)
    private fun findPostBySlugOrId(slugOrId: String): Post? {
        val post = postService.findPostBySlugOrId(slugOrId)
        if (Objects.isNull(post)) {
            log.error("Post is not found.")
            throw ServerException(OmaErrorMessageType.NOT_FOUND, arrayOf("Post: $slugOrId"), HttpStatus.NOT_FOUND)
        }
        return post
    }

    /**
     * @param id -- identifier of Comment entity
     * @return -- Comment entity that is retrieved from db
     * @throws ServerException -- if comment is not found
     */
    @Throws(ServerException::class)
    private fun findComment(id: String): Comment {
        val comment: Comment? = commentService.findById(id)
        if (Objects.isNull(comment)) {
            log.error("Comment is not found.")
            throw ServerException(OmaErrorMessageType.NOT_FOUND, arrayOf("Comment: $id"), HttpStatus.NOT_FOUND)
        }
        return comment!!
    }

    /**
     * @param username -- slug or identifier of User entity
     * @return -- User entity that is retrieved from db
     */
    private fun findUser(username: String): User? {
        return userService.findByUsername(username)
    }

    companion object {
        const val URL = "/api/v1/posts"
    }
}
