package com.github.senocak.util.validation

import com.github.senocak.domain.dto.user.UpdateUserDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class PasswordMatchesValidator : ConstraintValidator<PasswordMatches, Any> {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun initialize(passwordMatches: PasswordMatches) {
        log.info("PasswordMatchesValidator initialized")
    }

    override fun isValid(obj: Any, context: ConstraintValidatorContext): Boolean {
        if (obj.javaClass == UpdateUserDto::class.java) {
            val (_, password, password_confirmation) = obj as UpdateUserDto
            return password == password_confirmation
        }
        return false
    }
}