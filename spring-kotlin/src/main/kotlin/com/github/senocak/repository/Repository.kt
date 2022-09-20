package com.github.senocak.repository

import com.github.senocak.domain.Category
import com.github.senocak.domain.Comment
import com.github.senocak.domain.Post
import com.github.senocak.domain.Role
import com.github.senocak.domain.User
import com.github.senocak.util.RoleName
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Date
import java.util.Optional

@Repository
interface CategoryRepository: PagingAndSortingRepository<Category, String>, JpaSpecificationExecutor<Category> {
    @Query(value = "SELECT p FROM Category p WHERE p.id = :idOrSlug or p.slug = :idOrSlug")
    fun findByIdOrSlug(@Param("idOrSlug") idOrNameOrSlug: String): Optional<Category?>
}

@Repository
interface CommentRepository: PagingAndSortingRepository<Comment, String>

@Repository
interface PostRepository: JpaRepository<Post, String>, JpaSpecificationExecutor<Post> {
    @Query(value = "SELECT p FROM Post p WHERE p.id = :slug or p.slug = :slug ")
    fun findPostBySlugOrId(@Param("slug") slug: String): Post?
    fun findByBodyIsLikeOrTitleIsLike(body: String, title: String): List<Post?>?
    fun findByCreatedAtBetween(from: Date, to: Date): List<Post?>? //Find posts between today and 3 days ahead
}

@Repository
interface RoleRepository: PagingAndSortingRepository<Role, Long> {
    fun findByName(roleName: RoleName): Optional<Role?>
}

@Repository
interface UserRepository: PagingAndSortingRepository<User, String> {
    fun findByUsernameOrEmail(username: String?, email: String?): Optional<User?>?
    fun findByIdIn(userIds: List<String?>?): List<User?>?
    fun findByEmail(email: String?): Optional<User?>?
    fun findByUsername(username: String?): Optional<User?>?
    fun existsByUsername(username: String?): Boolean
    fun existsByEmail(email: String?): Boolean
}