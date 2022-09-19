package com.github.senocak.util.validation

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext


class EmailValidator : ConstraintValidator<ValidEmail?, String> {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun initialize(constraintAnnotation: ValidEmail?) {
        log.info("EmailValidator initialized")
    }

    override fun isValid(email: String, context: ConstraintValidatorContext): Boolean {
        return validateEmail(email)
    }

    private fun validateEmail(email: String): Boolean {
        val pattern: Pattern = Pattern.compile(
            "^[_A-Za-z0-9-+]" +
                    "(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*" + "(.[A-Za-z]{2,})$"
        )
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }

    fun isValidEmailAddress(email: String?): Boolean {
        log.info("Email is validation started.")
        val ePattern =
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$"
        val p = Pattern.compile(ePattern)
        val m = p.matcher(email)
        return m.matches()
    }
}