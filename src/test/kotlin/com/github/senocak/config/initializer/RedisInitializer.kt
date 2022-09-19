package com.github.senocak.config.initializer

import com.github.senocak.TestConstants
import org.junit.Assert
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import redis.clients.jedis.Jedis

@TestConfiguration
class RedisInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {}

    companion object {
        private var jedis: Jedis? = null
        private const val redisPort = 6379
        private const val REDIS_LOG_REGEX = ".*Ready to accept connections.*"

        @Container
        var redisContainer: GenericContainer<*>? = null

        @Rule
        var env = EnvironmentVariables()

        init {
            redisContainer = GenericContainer("redis:6.2-alpine")
                .withExposedPorts(redisPort)
                .withStartupTimeout(TestConstants.CONTAINER_WAIT_TIMEOUT)
                .waitingFor(Wait.forLogMessage(REDIS_LOG_REGEX, 1))
            redisContainer!!.start()
            val REDIS_HOST = redisContainer!!.containerIpAddress
            val REDIS_PORT = redisContainer!!.firstMappedPort
            env["REDIS_HOST"] = REDIS_HOST
            env["REDIS_PORT"] = REDIS_PORT.toString()
            env["REDIS_PASSWORD"] = ""
            jedis = Jedis(REDIS_HOST, REDIS_PORT)
            Assert.assertNotNull(jedis)
            setInitialValuesRedis()
        }

        private fun setInitialValuesRedis() {
            jedis!!.configSet("notify-keyspace-events", "KEA")
//            val emailConfig: MutableMap<String, String> = HashMap()
//            emailConfig["protocol"] = TestConstants.emailConfig.getProtocol()
//            emailConfig["host"] = TestConstants.emailConfig.getHost()
//            emailConfig["port"] = java.lang.String.valueOf(TestConstants.emailConfig.getPort())
//            emailConfig["from"] = TestConstants.emailConfig.getFrom()
//            emailConfig["password"] = TestConstants.emailConfig.getPassword()
//            jedis!!.hmset("email", emailConfig)
        }
    }
}
