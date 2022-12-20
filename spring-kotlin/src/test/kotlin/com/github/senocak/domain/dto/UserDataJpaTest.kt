package com.github.senocak.domain.dto

import com.github.senocak.repository.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension


@DataJpaTest
@ExtendWith(SpringExtension::class)
@ActiveProfiles(value = ["datajpa-test"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
/*
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:tc:mysql:8.0.1://localhost/spring?TC_INITSCRIPT=file:src/test/resources/db.sql",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
    "spring.liquibase.enabled=false",
    "spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect",
    "spring.jpa.hibernate.ddl-auto=validate"
])
*/
class UserDataJpaTest {
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    //@Sql("/db.sql")
    fun test() {
        Assertions.assertEquals(2,userRepository.findAll().count())

        val queryForList = jdbcTemplate.queryForList("select * from users")
        Assertions.assertEquals(2,queryForList.size)
    }
}