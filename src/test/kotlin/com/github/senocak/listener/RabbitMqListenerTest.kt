package com.github.senocak.listener

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.domain.dto.Mail
import com.github.senocak.domain.dto.rabbitmq.ServiceData
import com.github.senocak.domain.dto.user.UserResponse
import com.github.senocak.service.EmailService
import com.github.senocak.util.Action
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.thymeleaf.ITemplateEngine

@Tag("unit")
@ExtendWith(MockitoExtension::class)
@DisplayName("Unit Tests for RabbitMqListener")
class RabbitMqListenerTest {
    private val objectMapper: ObjectMapper = Mockito.mock(ObjectMapper::class.java)
    private val emailService: EmailService = Mockito.mock(EmailService::class.java)
    private val iTemplateEngine: ITemplateEngine = Mockito.mock(ITemplateEngine::class.java)
    private var rabbitMqListener: RabbitMqListener = RabbitMqListener(objectMapper, emailService, iTemplateEngine)

    @Test
    @Throws(JsonProcessingException::class)
    fun givenMe_WhenReceiveMessage_ThenVerify() {
        // Given
        val serviceData = ServiceData()
        serviceData.message = "Hello World"
        serviceData.action = Action.Me
        Mockito.doReturn(serviceData).`when`(objectMapper).readValue("serviceData", ServiceData::class.java)
        Mockito.doReturn(UserResponse("", "", "", HashSet(), ""))
            .`when`(objectMapper).readValue(serviceData.message, UserResponse::class.java)
        // When
        rabbitMqListener.receiveMessage("serviceData")
        // Then
        Mockito.verify(emailService).sendMail(any<Mail>())
    }
}
