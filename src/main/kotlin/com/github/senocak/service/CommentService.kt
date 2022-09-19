package com.github.senocak.service

import com.github.senocak.domain.Comment
import com.github.senocak.repository.CommentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CommentService(private val commentRepository: CommentRepository) {

    /**
     * @param nextPage -- next page variable to filter
     * @param maxNumber -- max page to retrieve from db
     * @return -- comment objects that has retrieved by page
     */
    fun getAll(nextPage: Int, maxNumber: Int): Page<Comment> {
        val paging: Pageable = PageRequest.of(nextPage, maxNumber)
        return commentRepository.findAll(paging)
    }

    /**
     * @param id -- identifier of the comment entity
     * @return -- A Comment entity retrieved from db
     */
    fun findById(id: String): Comment {
        return commentRepository.findById(id).orElse(null)
    }

    /**
     * @param comment -- a Comment entity to persist
     * @return -- persisted Comment entity
     */
    fun persist(comment: Comment): Comment {
        return commentRepository.save(comment)
    }
}