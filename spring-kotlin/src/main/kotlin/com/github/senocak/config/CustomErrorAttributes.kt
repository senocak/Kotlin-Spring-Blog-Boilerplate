package com.github.senocak.config

import com.github.senocak.domain.ExceptionDto
import com.github.senocak.util.OmaErrorMessageType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.WebRequest
import javax.servlet.RequestDispatcher

@Configuration
@Profile("!integration-test")
class CustomErrorAttributes : DefaultErrorAttributes() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun getErrorAttributes(webRequest: WebRequest, options: ErrorAttributeOptions): Map<String, Any> {
        val errorAttributes = super.getErrorAttributes(webRequest, options)
        val errorMessage = webRequest.getAttribute(RequestDispatcher.ERROR_MESSAGE, RequestAttributes.SCOPE_REQUEST)
        val exceptionDto = ExceptionDto()
        if (errorMessage != null) {
            val omaErrorMessageType = OmaErrorMessageType.NOT_FOUND
            exceptionDto.statusCode = errorAttributes["status"] as Int
            exceptionDto.variables = arrayOf(errorAttributes["error"].toString(), errorAttributes["message"].toString())
            exceptionDto.error = ExceptionDto.OmaErrorMessageTypeDto(omaErrorMessageType.messageId, omaErrorMessageType.text)
        }
        val map: MutableMap<String, Any> = HashMap()
        map["exception"] = exceptionDto
        log.debug("Exception occurred in DefaultErrorAttributes: $map")
        return map
    }
}