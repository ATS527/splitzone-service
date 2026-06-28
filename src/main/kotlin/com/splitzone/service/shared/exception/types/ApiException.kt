package com.splitzone.service.shared.exception.types

import org.springframework.http.HttpStatus

open class ApiException(
    val status: HttpStatus,
    val code: String,
    override val message: String
) : RuntimeException(message)

class ResourceNotFoundException(
    resourceName: String,
    identifier: String
) : ApiException(
    status = HttpStatus.NOT_FOUND,
    code = "RESOURCE_NOT_FOUND",
    message = "$resourceName with identifier '$identifier' was not found"
)
