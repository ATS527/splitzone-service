package com.example.splitzone.service.shared.exception.handler

import com.example.splitzone.service.shared.exception.dto.ApiErrorResponse
import com.example.splitzone.service.shared.exception.dto.FieldValidationError
import com.example.splitzone.service.shared.exception.filter.RequestIdFilter
import com.example.splitzone.service.shared.exception.types.ApiException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.resource.NoResourceFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        exception: ApiException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        logClientError(exception.status, exception.code, exception.message, request)
        return buildResponse(
            status = exception.status,
            code = exception.code,
            message = exception.message,
            request = request
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        exception: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        val fieldErrors = exception.constraintViolations.map {
            FieldValidationError(
                field = it.propertyPath.toString(),
                message = it.message
            )
        }
        logClientError(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", exception.message ?: "Constraint violation", request)
        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            code = "CONSTRAINT_VIOLATION",
            message = "Request validation failed",
            request = request,
            fieldErrors = fieldErrors
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        log.error(
            "Unhandled exception. requestId={}, path={}",
            requestId(request),
            request.requestURI,
            exception
        )
        return buildResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred. Please contact support with the requestId.",
            request = request
        )
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val servletRequest = servletRequest(request)
            ?: return requireNotNull(super.handleMethodArgumentNotValid(ex, headers, status, request))

        val fieldErrors = ex.bindingResult.allErrors.mapNotNull { error ->
            when (error) {
                is FieldError -> FieldValidationError(error.field, error.defaultMessage ?: "Invalid value")
                else -> null
            }
        }

        logClientError(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request body validation failed", servletRequest)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .header(RequestIdFilter.REQUEST_ID_HEADER, requestId(servletRequest))
            .body(
                errorResponse(
                    status = HttpStatus.BAD_REQUEST,
                    code = "VALIDATION_FAILED",
                    message = "Request validation failed",
                    request = servletRequest,
                    fieldErrors = fieldErrors
                )
            )
    }

    override fun handleHandlerMethodValidationException(
        ex: HandlerMethodValidationException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val servletRequest = servletRequest(request)
            ?: return requireNotNull(super.handleHandlerMethodValidationException(ex, headers, status, request))

        val fieldErrors = ex.parameterValidationResults.flatMap { validationResult ->
            validationResult.resolvableErrors.map { error ->
                FieldValidationError(
                    field = validationResult.methodParameter.parameterName ?: "parameter",
                    message = error.defaultMessage ?: "Invalid value"
                )
            }
        }

        logClientError(HttpStatus.BAD_REQUEST, "METHOD_VALIDATION_FAILED", "Method validation failed", servletRequest)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .header(RequestIdFilter.REQUEST_ID_HEADER, requestId(servletRequest))
            .body(
                errorResponse(
                    status = HttpStatus.BAD_REQUEST,
                    code = "METHOD_VALIDATION_FAILED",
                    message = "Request validation failed",
                    request = servletRequest,
                    fieldErrors = fieldErrors
                )
            )
    }

    override fun handleNoResourceFoundException(
        ex: NoResourceFoundException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val servletRequest = servletRequest(request)
            ?: return requireNotNull(super.handleNoResourceFoundException(ex, headers, status, request))

        logClientError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "No handler found for request", servletRequest)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .header(RequestIdFilter.REQUEST_ID_HEADER, requestId(servletRequest))
            .body(
                errorResponse(
                    status = HttpStatus.NOT_FOUND,
                    code = "RESOURCE_NOT_FOUND",
                    message = "The requested resource was not found",
                    request = servletRequest
                )
            )
    }

    private fun buildResponse(
        status: HttpStatus,
        code: String,
        message: String,
        request: HttpServletRequest,
        fieldErrors: List<FieldValidationError> = emptyList()
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(status)
            .header(RequestIdFilter.REQUEST_ID_HEADER, requestId(request))
            .body(errorResponse(status, code, message, request, fieldErrors))

    private fun errorResponse(
        status: HttpStatus,
        code: String,
        message: String,
        request: HttpServletRequest,
        fieldErrors: List<FieldValidationError> = emptyList()
    ) = ApiErrorResponse(
        timestamp = Instant.now(),
        status = status.value(),
        error = status.reasonPhrase,
        code = code,
        message = message,
        path = request.requestURI,
        requestId = requestId(request),
        fieldErrors = fieldErrors
    )

    private fun requestId(request: HttpServletRequest): String =
        request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE)?.toString() ?: "unknown"

    private fun servletRequest(request: WebRequest): HttpServletRequest? =
        (request as? ServletWebRequest)?.request

    private fun logClientError(
        status: HttpStatus,
        code: String,
        message: String,
        request: HttpServletRequest
    ) {
        log.warn(
            "Request failed. status={}, code={}, requestId={}, path={}, message={}",
            status.value(),
            code,
            requestId(request),
            request.requestURI,
            message
        )
    }
}
