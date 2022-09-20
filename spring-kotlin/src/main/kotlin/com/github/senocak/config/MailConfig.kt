package com.github.senocak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.spring5.SpringTemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import java.util.Properties

@Configuration
class MailConfig {
    @Value("\${mail.smtp.socketFactory.port}") private val socketPort = 0
    @Value("\${mail.smtp.auth}") private val auth = false
    @Value("\${mail.smtp.starttls.enable}") private val starttls = false
    @Value("\${mail.smtp.starttls.required}") private val startlls_required = false
    @Value("\${mail.smtp.socketFactory.fallback}") private val fallback = false

    @Value("\${mail.host}") private val host: String? = null
    @Value("\${mail.port}") private val port: Int = 0
    @Value("\${mail.protocol}") private val protocol: String? = null
    @Value("\${mail.username}") private val username: String? = null
    @Value("\${mail.password}") private val password: String? = null

    /**
     * Defining JavaMailSender as a bean
     * JavaMailSender is an interface for JavaMail, supporting MIME messages both as direct arguments
     * and through preparation callbacks
     * @return -- an implementation of the JavaMailSender interface
     */
    @Bean
    fun javaMailSender(): JavaMailSender {
        val mailProperties = Properties()
        mailProperties["mail.smtp.auth"] = auth
        mailProperties["mail.smtp.starttls.enable"] = starttls
        mailProperties["mail.smtp.starttls.required"] = startlls_required
        mailProperties["mail.smtp.socketFactory.port"] = socketPort
        mailProperties["mail.smtp.debug"] = "true"
        mailProperties["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        mailProperties["mail.smtp.socketFactory.fallback"] = fallback
        val mailSender = JavaMailSenderImpl()
        mailSender.javaMailProperties = mailProperties
        mailSender.host = host
        mailSender.port = port
        mailSender.protocol = protocol
        mailSender.username = username
        mailSender.password = password
        return mailSender
    }

    /**
     * THYMELEAF TemplateResolver(3) <- TemplateEngine
     */
    @Bean(name = ["htmlTemplateEngine"])
    fun htmlTemplateEngine(): ITemplateEngine {
        val templateEngine = SpringTemplateEngine()
        templateEngine.addTemplateResolver(htmlTemplateResolver())
        templateEngine.setTemplateEngineMessageSource(templateMessageSource())
        return templateEngine
    }

    /**
     * THYMELEAF TemplateResolver(3) <- TemplateEngine
     * @return -- an implementation of the ITemplateResolver interface
     */
    private fun htmlTemplateResolver(): ITemplateResolver {
        val templateResolver = ClassLoaderTemplateResolver()
        templateResolver.order = 2
        templateResolver.prefix = URL_TEMPLATES
        templateResolver.suffix = HTML_EXTENSION
        templateResolver.templateMode = TemplateMode.HTML
        templateResolver.characterEncoding = EMAIL_TEMPLATE_ENCODING
        templateResolver.isCacheable = false
        return templateResolver
    }

    /**
     * THYMELEAF TemplateMessageSource
     * @return -- an implementation of the TemplateMessageSource interface
     */
    private fun templateMessageSource(): ResourceBundleMessageSource {
        val messageSource = ResourceBundleMessageSource()
        messageSource.setBasename("templates/i18n/Template")
        return messageSource
    }

    companion object {
        const val EMAIL_TEMPLATE_ENCODING = "UTF-8"
        const val URL_TEMPLATES = "/templates/"
        const val HTML_EXTENSION = ".html"
    }
}