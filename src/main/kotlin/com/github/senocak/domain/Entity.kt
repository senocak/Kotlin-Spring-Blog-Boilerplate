package com.github.senocak.domain

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.github.senocak.util.AppConstants
import com.github.senocak.util.RoleName
import org.hibernate.annotations.GenericGenerator
import java.io.Serializable
import java.util.Date
import java.util.Objects
import java.util.UUID
import javax.persistence.AttributeConverter
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Converter
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.Lob
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.persistence.OneToMany
import javax.persistence.PrePersist
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@MappedSuperclass
open class BaseDomain(
        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        @Column(name = "id", updatable = false, nullable = false)
        var id: String? = null,
        @Column var createdAt: Date = Date(),
        @Column var updatedAt: Date = Date()
): Serializable {
        @PrePersist
        protected open fun prePersist() {
                id = UUID.randomUUID().toString()
                // TODO: add snowflake
        }
}

@Entity
@Table(name = "users", uniqueConstraints = [
        UniqueConstraint(columnNames = ["username"]),
        UniqueConstraint(columnNames = ["email"])
])
class User(
        @Column var name: String,
        @Column var username: String,
        @Column var email : String,
        @Column var password : String?,
        @JoinTable(name = "user_roles",
                joinColumns = [JoinColumn(name = "user_id")],
                inverseJoinColumns = [JoinColumn(name = "role_id")]
        ) @ManyToMany(fetch = FetchType.LAZY)
        var roles: Set<Role> = HashSet()
): BaseDomain()

@Entity
@Table(name = "roles")
class Role(@Column @Enumerated(EnumType.STRING) var name: RoleName? = null): BaseDomain()

@Entity
@Table(name = "categories", uniqueConstraints = [UniqueConstraint(columnNames = ["slug"])])
class Category(
        @Column(nullable = false) var name: String? = null,
        @Column(nullable = false) var slug: String? = null,
        @Column(nullable = false) @Lob var image: String? = null,
        @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY) var posts: MutableList<Post>? =  null
): BaseDomain() {
        override fun prePersist() {
                super.prePersist()
                slug = AppConstants.toSlug(name!!)
        }
}

@Entity
@Table(name = "posts")
class Post(
        @Column(nullable = false) var title: String? = null,
        @Column(nullable = false, name = "slug") var slug: String? = null,
        @Lob @Column(nullable = false) var body: String? = null,
        @ManyToOne var user: User? = null,
        @ManyToMany
        @JoinTable(name = "post_category",
                joinColumns = [JoinColumn(name = "post_id")],
                inverseJoinColumns = [JoinColumn(name = "category_id")])
        var categories: MutableList<Category>? = null,
        @JsonBackReference
        @OneToMany(cascade = [CascadeType.ALL], mappedBy = "post", fetch = FetchType.LAZY)
        var comments: MutableList<Comment>? = null,
        @Convert(converter = StringListConverter::class)
        var tags: List<String>? = null
): BaseDomain(){
        override fun prePersist() {
                super.prePersist()
                slug = AppConstants.toSlug(title!!)
        }
}

@Entity
@Table(name = "comments")
class Comment(
        @Column var name: String? = null,
        @Column var email: String? = null,
        @Column var body: String? = null,
        @Column var approved: Boolean = false,
        @ManyToOne @JsonManagedReference var post: Post? = null
): BaseDomain()

@Converter
class StringListConverter : AttributeConverter<List<String?>?, String?> {
        private val splitChar = ";"
        override fun convertToDatabaseColumn(stringList: List<String?>?): String? {
                if (Objects.nonNull(stringList) && stringList!!.isNotEmpty())
                        return java.lang.String.join(splitChar, stringList)
                return null
        }

        override fun convertToEntityAttribute(string: String?): List<String?>? {
                if (Objects.nonNull(string))
                        return listOf(*string!!.split(splitChar.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                return null
        }
}
