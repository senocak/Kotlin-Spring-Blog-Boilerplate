package com.github.senocak.service

import com.github.senocak.domain.dto.Mail
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(private val emailSender: JavaMailSender){
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Send email.
     * @param mail Mail object
     */
    fun sendMail(mail: Mail) {
        val msg = emailSender.createMimeMessage()
        try {
            val mimeMessageHelper = MimeMessageHelper(msg, true)
            mimeMessageHelper.setTo(mail.to!!)
            mimeMessageHelper.setFrom(mail.from!!)
            mimeMessageHelper.setSubject(mail.subject!!)
            mimeMessageHelper.setText(mail.content!!, true)
            emailSender.send(msg)
        } catch (e: Exception) {
            log.error("Error while generating email message: {}", ExceptionUtils.getStackTrace(e))
        }
    }
}