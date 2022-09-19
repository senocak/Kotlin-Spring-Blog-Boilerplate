package com.github.senocak.config.initializer

import com.github.senocak.TestConstants
import org.junit.Rule
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.containers.MySQLContainer
import org.junit.contrib.java.lang.system.EnvironmentVariables

@TestConfiguration
class MysqlInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
//        TestPropertyValues.of(
//            "spring.datasource.url=" + MYSQL_CONTAINER!!.jdbcUrl,
//            "spring.datasource.username=" + MYSQL_CONTAINER!!.username,
//            "spring.datasource.password=" + MYSQL_CONTAINER!!.password
//        ).applyTo(configurableApplicationContext.environment)
    }

    companion object {
        @Container private var MYSQL_CONTAINER: MySQLContainer<*>? = null
        @Rule private val env: EnvironmentVariables = EnvironmentVariables()

        init {
            MYSQL_CONTAINER = MySQLContainer("mysql:8.0.1")
                .withExposedPorts(3306)
                .withDatabaseName("spring")
                .withUsername("root")
                .withPassword("root")
                .withInitScript("db.sql")
                .withStartupTimeout(TestConstants.CONTAINER_WAIT_TIMEOUT)
            MYSQL_CONTAINER!!.start()
            env.set("MYSQL_HOST", MYSQL_CONTAINER!!.containerIpAddress)
            env.set("MYSQL_PORT", MYSQL_CONTAINER!!.getMappedPort(3306).toString())
            env.set("MYSQL_DB", MYSQL_CONTAINER!!.databaseName)
            env.set("MYSQL_USER", MYSQL_CONTAINER!!.username)
            env.set("MYSQL_PASSWORD", MYSQL_CONTAINER!!.password)
        }
    }
}