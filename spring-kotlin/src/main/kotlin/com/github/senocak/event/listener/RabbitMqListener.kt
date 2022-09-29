package com.github.senocak.event.listener

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.domain.dto.Mail
import com.github.senocak.domain.dto.rabbitmq.ServiceData
import com.github.senocak.domain.dto.user.UserResponse
import com.github.senocak.service.EmailService
import com.github.senocak.util.Action
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import java.util.Locale

@Component
class RabbitMqListener(
    private val objectMapper: ObjectMapper,
    private val emailService: EmailService,
    private val htmlTemplateEngine: ITemplateEngine){
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @RabbitListener(queues = ["\${app.rabbitmq.QUEUE}"])
    fun receiveMessage(data: String?) {
        log.info("Message received: {}", data)
        val serviceData: ServiceData
        try {
            serviceData = objectMapper.readValue(data, ServiceData::class.java)
        } catch (e: JsonProcessingException) {
            log.error("Error while parsing json: {}", e.message)
            return
        }
        when (serviceData.action) {
            Action.Login -> loginAction(serviceData.message!!)
            Action.Me -> generateMeMail(serviceData.message!!)
            Action.Logout -> logoutAction(serviceData.message!!)
            else -> {}
        }
    }

    /**
     * Generate me mail.
     * @param message message
     */
    private fun generateMeMail(message: String) {
        val userResponse: UserResponse
        try {
            userResponse = objectMapper.readValue(message, UserResponse::class.java)
        } catch (e: JsonProcessingException) {
            log.error("Error while parsing json: {}", e.message)
            return
        }
        val ctx = Context(Locale("en"))
        ctx.setVariable("user", userResponse)
        val getHTMLTemplate = htmlTemplateEngine.process("welcome", ctx)
        val mail = Mail()
        mail.from = "configService.getEmailHash().getFrom()"
        mail.to = "senocakanil@gmail.com"
        mail.content = getHTMLTemplate
        mail.subject = "New Login Attempt"
        emailService.sendMail(mail)
    }

    /**
     * Login action.
     * @param message message
     */
    private fun loginAction(message: String) {
        log.info("Login action: {}", message)
    }

    /**
     * Logout action.
     * @param message message
     */
    private fun logoutAction(message: String) {
        log.info("Logout action: {}", message)
    }
}
