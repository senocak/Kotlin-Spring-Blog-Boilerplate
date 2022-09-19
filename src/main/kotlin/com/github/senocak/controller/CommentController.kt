package com.github.senocak.controller

import com.github.senocak.domain.Comment
import com.github.senocak.domain.ExceptionDto
import com.github.senocak.domain.dto.comment.CommentDto
import com.github.senocak.domain.dto.comment.CommentListDto
import com.github.senocak.domain.dto.comment.CommentWrapperDto
import com.github.senocak.exception.ServerException
import com.github.senocak.security.Authorize
import com.github.senocak.service.CommentService
import com.github.senocak.service.DtoConverter
import com.github.senocak.util.AppConstants
import com.github.senocak.util.AppConstants.ADMIN
import com.github.senocak.util.AppConstants.securitySchemeName
import com.github.senocak.util.OmaErrorMessageType
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
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import java.util.Objects
import java.util.stream.Collectors
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@Validated
@RestController
@RequestMapping(CommentController.URL)
@Tag(name = "Comment", description = "Comment API")
class CommentController(private val commentService: CommentService): BaseController() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    @Authorize(roles = [ADMIN])
    @Operation(
        summary = "Get All Comments",
        tags = ["Comment"],
        responses = [
            ApiResponse(responseCode = "201", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = CommentListDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN])]
    )
    fun getAll(
        @Parameter(description = "Number of resources that is requested.", required = true)
            @RequestParam(value = "next", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) @Max(99) nextPage: Int,
        @Parameter(description = "Pointer for the next page to retrieve.", required = true)
            @RequestParam(value = "max", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(0) @Max(99) maxNumber: Int
    ): ResponseEntity<CommentListDto> {
        val comments: Page<Comment> = commentService.getAll(nextPage, maxNumber)
        val dtos: List<CommentDto> = comments.content.stream()
            .map(DtoConverter::convertEntityToDto)
            .collect(Collectors.toList())
        val commentListDto = CommentListDto(dtos, if (comments.hasNext()) (nextPage + 1).toLong() else 0,
            comments.totalElements, URL)
        return ResponseEntity.ok(commentListDto)
    }

    @DeleteMapping(value = ["/{resourceId}"])
    @Authorize(roles = [ADMIN])
    @Operation(
        summary = "Delete Comment",
        tags = ["Comment"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json"))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN])]
    )
    @Throws(ServerException::class)
    fun delete(
        @Parameter(description = "Identifier of the comment", required = true) @PathVariable resourceId: String
    ): ResponseEntity<Void> {
        val comment: Comment = findComment(resourceId)
        commentService.persist(comment)
        log.info("Comment: {} is deleted", comment.name)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping(value = ["/{resourceId}"])
    @Authorize(roles = [ADMIN])
    @Operation(
        summary = "Update Comment",
        tags = ["Comment"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = CommentWrapperDto::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN])]
    )
    @Throws(ServerException::class)
    fun update(
        @Parameter(description = "Identifier of the comment", required = true) @PathVariable resourceId: String,
        @Parameter(description = "Status of the comment", required = true) @RequestParam status: Boolean
    ): ResponseEntity<CommentWrapperDto> {
        val comment: Comment = findComment(resourceId)
        comment.approved = status
        commentService.persist(comment)
        val commentDto: CommentDto = DtoConverter.convertEntityToDto(comment)
        log.info("Comment is update: {}", commentDto)
        return ResponseEntity.ok(CommentWrapperDto(commentDto))
    }

    /**
     * @param resourceId -- slug or identifier of Category entity
     * @return -- Comment entity that is retrieved from db
     * @throws ServerException -- if Comment is not found
     */
    @Throws(ServerException::class)
    private fun findComment(resourceId: String): Comment {
        val comment: Comment = commentService.findById(resourceId)
        if (Objects.isNull(comment)) {
            log.error("Comment is not found.")
            throw ServerException(OmaErrorMessageType.NOT_FOUND, arrayOf("Comment: $resourceId"), HttpStatus.NOT_FOUND)
        }
        return comment
    }

    companion object {
        const val URL = "/api/v1/comments"
    }
}
