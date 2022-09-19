package com.github.senocak.config.initializer

import com.github.senocak.TestConstants
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import org.junit.Assert
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import java.io.IOException
import java.util.concurrent.TimeoutException

@TestConfiguration
class RabbitMqInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        assert(RABBIT_MQ_CONTAINER != null)
        TestPropertyValues.of(
                "spring.rabbitmq.host=" + RABBIT_MQ_CONTAINER!!.containerIpAddress,
                "spring.rabbitmq.port=" + RABBIT_MQ_CONTAINER!!.getMappedPort(RABBIT_MQ_PORT)
            )
            .applyTo(configurableApplicationContext.environment)
    }

    companion object {
        private const val RABBIT_MQ_PORT = 5672
        var rabbitmq: Channel? = null

        @Container
        private var RABBIT_MQ_CONTAINER: GenericContainer<*>? = null

        @Rule
        var env = EnvironmentVariables()

        init {
            RABBIT_MQ_CONTAINER = GenericContainer("rabbitmq:3.6-management-alpine")
                .withExposedPorts(RABBIT_MQ_PORT)
                .withEnv("RABBITMQ_IO_THREAD_POOL_SIZE", "4")
                .withStartupTimeout(TestConstants.CONTAINER_WAIT_TIMEOUT)
                .waitingFor(Wait.forListeningPort())
            RABBIT_MQ_CONTAINER!!.start()
            val RABBIT_HOST = RABBIT_MQ_CONTAINER!!.containerIpAddress
            val RABBIT_PORT = RABBIT_MQ_CONTAINER!!.getMappedPort(RABBIT_MQ_PORT)
            val RABBIT_USERNAME = "guest"
            val RABBIT_PASSWD = "guest"
            env["RABBITMQ_HOST"] = RABBIT_HOST
            env["RABBITMQ_PORT"] = RABBIT_PORT.toString()
            env["RABBITMQ_USER"] = RABBIT_USERNAME
            env["RABBITMQ_SECRET"] = RABBIT_PASSWD
            val factory = ConnectionFactory()
            factory.host = RABBIT_HOST
            factory.port = RABBIT_PORT
            factory.username = RABBIT_USERNAME
            factory.password = RABBIT_PASSWD
            try {
                factory.newConnection().use { connection ->
                    rabbitmq = connection.createChannel()
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (e: TimeoutException) {
                throw RuntimeException(e)
            }
            Assert.assertNotNull(rabbitmq)
        }
    }
}