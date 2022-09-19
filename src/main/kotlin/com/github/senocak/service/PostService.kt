package com.github.senocak.service

import com.github.senocak.domain.Category
import com.github.senocak.domain.Post
import com.github.senocak.domain.User
import com.github.senocak.repository.PostRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Root

@Service
class PostService(private val postRepository: PostRepository) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * @param user -- user entity that posts has relation
     * @param category -- category entity that posts has relation
     * @param nextPage -- next page variable to filter
     * @param maxNumber -- max page to retrieve from db
     * @return -- post objects that has retrieved by page
     */
    @Transactional
    fun getAllByUser(user: User?, category: Category?, nextPage: Int, maxNumber: Int): Page<Post> {
        val paging: Pageable = PageRequest.of(nextPage, maxNumber)
        val specification: Specification<Post> =
            Specification { root: Root<Post>, _: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder ->
                var userPredicate = criteriaBuilder.equal(criteriaBuilder.literal(1), 1)
                if (user != null)
                    userPredicate = criteriaBuilder.equal(root.get<User>("user"), user)

                var categoryPredicate = criteriaBuilder.equal(criteriaBuilder.literal(1), 1)
                if (category != null)
                    categoryPredicate = criteriaBuilder.equal(root.join<Category, Post>("categories", JoinType.LEFT), category)
                criteriaBuilder.and(userPredicate, categoryPredicate)
            }
        return postRepository.findAll(specification, paging)
    }

    /**
     * @param slug -- String variable that is slug
     * @return -- Post object that is Post object
     */
    fun findPostBySlugOrId(slug: String): Post? {
        log.info("Getting post from slug: {}", slug)
        return postRepository.findPostBySlugOrId(slug)
    }

    /**
     * @param post -- Post object to persist/update db
     * @return -- Post object that is persisted to db
     */
    fun persist(post: Post): Post {
        return postRepository.save(post)
    }
}