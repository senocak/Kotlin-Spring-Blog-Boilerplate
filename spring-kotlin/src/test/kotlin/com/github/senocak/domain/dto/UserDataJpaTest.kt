package com.github.senocak.domain.dto

import com.github.senocak.repository.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@DataJpaTest
@ExtendWith(SpringExtension::class)
@ActiveProfiles(value = ["datajpa-test"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserDataJpaTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    //@Sql("/db.sql")
    fun test() {
        Assertions.assertEquals(2,userRepository.findAll().count())
    }
}