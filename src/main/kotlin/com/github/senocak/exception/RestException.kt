package com.github.senocak.exception

import com.github.senocak.util.OmaErrorMessageType
import org.springframework.http.HttpStatus

open class RestException(msg: String, t: Throwable? = null): Exception(msg, t)
class ServerException(var omaErrorMessageType: OmaErrorMessageType, var variables: Array<String?>, var statusCode: HttpStatus):
    RestException("OmaErrorMessageType: $omaErrorMessageType, variables: $variables, statusCode: $statusCode")

class ConfigException(t: Throwable):
    RestException("ConfigException: ${t.message}")